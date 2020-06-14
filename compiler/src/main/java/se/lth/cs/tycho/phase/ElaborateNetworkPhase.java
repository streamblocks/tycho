package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.interp.BasicInterpreter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.*;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;
import se.lth.cs.tycho.transformation.nl2network.NlToNetwork;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElaborateNetworkPhase implements Phase {
    @Override
    public String getDescription() {
        return "Elaborates the entities that are networks.";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        return task.withNetwork(fullyElaborate(task, context, task.getNetwork(), new HashSet<>(), Optional.empty()));
    }

    public static final Setting<Boolean> experimentalNetworkElaboration = new OnOffSetting() {
        @Override
        public String getKey() {
            return "experimental-network-elaboration";
        }

        @Override
        public String getDescription() {
            return "Experimental network elaboration supporting advanced CAL network languages features.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    @Override
    public List<Setting<?>> getPhaseSettings() {
        return Arrays.asList(experimentalNetworkElaboration);
    }

    public Network fullyElaborate(CompilationTask task, Context context, Network network, Set<String> names, Optional<ToolAttribute> partition) {
        Network result = uniqueNames(network, names);
        for (Instance instance : result.getInstances()) {
            GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, instance.getEntityName());
            Network elaborated;
            if (entity.getEntity() instanceof NlNetwork) {
                Optional<ToolAttribute> instPartition = partition.isPresent() ? partition : getPartitionAttribute(instance);
                if (context.getConfiguration().get(experimentalNetworkElaboration)) {
                    BasicInterpreter interpreter = new BasicInterpreter(task, 100);
                    NlToNetwork nlToNetwork = new NlToNetwork(task, (NlNetwork) entity.getEntity(), interpreter);
                    nlToNetwork.evaluate(instance.getValueParameters());
                    elaborated = nlToNetwork.getNetwork();
                } else {
                    elaborated = elaborate(context, (NlNetwork) entity.getEntity(), instPartition);
                }
                elaborated = fullyElaborate(task, context, elaborated, names, instPartition);
                result = connectElaboratedInstance(result, instance.getInstanceName(), elaborated);
            }
        }
        return result;
    }

    public Optional<ToolAttribute> getPartitionAttribute(Instance instance) {
        return instance.getAttributes().stream().filter(a -> a.getName().equals("partition")).findAny();
    }

    private Network uniqueNames(Network network, Set<String> names) {
        Map<String, String> dictionary = new HashMap<>();
        for (Instance instance : network.getInstances()) {
            String name = instance.getInstanceName();
            int i = 0;
            while (names.contains(name)) {
                name = instance.getInstanceName() + "_" + i++;
            }
            dictionary.put(instance.getInstanceName(), name);
            names.add(name);
        }
        ImmutableList<Instance> instances = network.getInstances().stream()
                .map(instance -> instance.withInstanceName(dictionary.get(instance.getInstanceName())))
                .collect(ImmutableList.collector());
        ImmutableList<Connection> connections = network.getConnections().stream()
                .map(connection -> {
                    Connection.End src = connection.getSource().withInstance(
                            connection.getSource().getInstance().map(dictionary::get));
                    Connection.End tgt = connection.getTarget().withInstance(
                            connection.getTarget().getInstance().map(dictionary::get));
                    return connection.copy(src, tgt);
                }).collect(ImmutableList.collector());
        return network.withInstances(instances).withConnections(connections);
    }

    @Override
    public Set<Class<? extends Phase>> dependencies() {
        return Collections.singleton(CreateNetworkPhase.class);
    }

    private Network connectElaboratedInstance(Network outer, String instanceName, Network inner) {
        assert inner.getConnections().stream().noneMatch(c ->
                !c.getSource().getInstance().isPresent() &&
                        !c.getTarget().getInstance().isPresent());

        assert inner.getInstances().stream()
                .map(Instance::getInstanceName)
                .noneMatch(outer.getInstances().stream()
                        .map(Instance::getInstanceName)
                        .collect(Collectors.toSet())::contains);

        Map<String, List<Connection>> incoming = inner.getConnections().stream()
                .filter(c -> !c.getSource().getInstance().isPresent())
                .collect(Collectors.groupingBy(c -> c.getSource().getPort()));
        Map<String, List<Connection>> outgoing = inner.getConnections().stream()
                .filter(c -> !c.getTarget().getInstance().isPresent())
                .collect(Collectors.groupingBy(c -> c.getTarget().getPort()));

        ImmutableList.Builder<Connection> builder = ImmutableList.builder();

        for (Connection connOuter : outer.getConnections()) {
            if (connOuter.getTarget().getInstance().equals(Optional.of(instanceName))) {
                Connection.End src = connOuter.getSource();
                String port = connOuter.getTarget().getPort();
                for (Connection connInner : incoming.getOrDefault(port, Collections.emptyList())) {
                    Connection.End tgt = connInner.getTarget();
                    builder.add(new Connection(src, tgt).withAttributes(mergeAttributes(connOuter, connInner)));
                }
            } else if (connOuter.getSource().getInstance().equals(Optional.of(instanceName))) {
                Connection.End tgt = connOuter.getTarget();
                String port = connOuter.getSource().getPort();
                for (Connection connInner : outgoing.getOrDefault(port, Collections.emptyList())) {
                    Connection.End src = connInner.getSource();
                    builder.add(new Connection(src, tgt).withAttributes(mergeAttributes(connOuter, connInner)));
                }
            } else {
                builder.add(connOuter);
            }
        }
        for (Connection connInner : inner.getConnections()) {
            if (connInner.getSource().getInstance().isPresent() && connInner.getTarget().getInstance().isPresent()) {
                builder.add(connInner);
            }
        }

        Stream<Instance> outerInstances = outer.getInstances().stream()
                .filter(instance -> !instance.getInstanceName().equals(instanceName));
        Stream<Instance> innerInstances = inner.getInstances().stream();

        ImmutableList<Instance> instances = Stream.concat(outerInstances, innerInstances)
                .collect(ImmutableList.collector());


        return new Network(outer.getAnnotations(), outer.getInputPorts(), outer.getOutputPorts(), instances, builder.build());
    }

    private List<ToolAttribute> mergeAttributes(Connection connSrc, Connection connTgt) {
        ImmutableList<ToolAttribute> attributes = ImmutableList.concat(connSrc.getAttributes(), connTgt.getAttributes());
        long count = attributes.stream()
                .map(ToolAttribute::getName)
                .distinct()
                .count();
        assert count == attributes.size();
        return attributes;
    }

    private Network elaborate(Context context, NlNetwork network, Optional<ToolAttribute> partition) {
//		assert network.getValueParameters().isEmpty();
//		assert network.getTypeParameters().isEmpty();
//		assert network.getVarDecls().isEmpty();
        Reporter reporter = context.getReporter();

        ImmutableList<PortDecl> inputPorts = network.getInputPorts().map(PortDecl::deepClone);
        ImmutableList<PortDecl> outputPorts = network.getOutputPorts().map(PortDecl::deepClone);

        ImmutableList.Builder<Instance> instances = ImmutableList.builder();
        for (InstanceDecl entity : network.getEntities()) {
            try {
                assert entity.getEntityExpr() instanceof EntityInstanceExpr;
                EntityInstanceExpr expr = (EntityInstanceExpr) entity.getEntityExpr();
                assert expr.getEntityName() instanceof EntityReferenceGlobal;
                QID entityName = ((EntityReferenceGlobal) expr.getEntityName()).getGlobalName();

                ImmutableList.Builder<ToolAttribute> attrs = ImmutableList.builder();
                if (partition.isPresent())
                    attrs.add(partition.get().deepClone());
                attrs.addAll(expr.getAttributes().map(ToolAttribute::deepClone));
                Instance instance = new Instance(
                        entity.getInstanceName(),
                        entityName,
                        expr.getValueParameters().map(ValueParameter::deepClone),
                        ImmutableList.empty())
                        .withAttributes(attrs.build());
                instances.add(instance);
            } catch (ClassCastException e) {
                reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Only Instance Entity expression are supported, compile with the following setting: --set experimental-network-elaboration=on"));
            }
        }

        ImmutableList.Builder<Connection> connections = ImmutableList.builder();
        for (StructureStatement stmt : network.getStructure()) {
            assert stmt instanceof StructureConnectionStmt;
            StructureConnectionStmt conn = (StructureConnectionStmt) stmt;
            assert conn.getSrc().getEntityIndex().isEmpty();
            assert conn.getDst().getEntityIndex().isEmpty();
            Connection connection = new Connection(convert(conn.getSrc()), convert(conn.getDst()))
                    .withAttributes(conn.getAttributes().map(ToolAttribute::deepClone));
            connections.add(connection);
        }
        return new Network(network.getAnnotations(), inputPorts, outputPorts, instances.build(), connections.build());
    }

    private Connection.End convert(PortReference portReference) {
        return new Connection.End(Optional.ofNullable(portReference.getEntityName()), portReference.getPortName());
    }
}
