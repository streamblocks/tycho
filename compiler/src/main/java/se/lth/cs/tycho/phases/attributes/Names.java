package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.ir.stmt.StmtWrite;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public interface Names {
	ModuleKey<Names> key = new ModuleKey<Names>() {
		@Override
		public Names createInstance(CompilationTask unit, AttributeManager manager) {
			return MultiJ.from(Implementation.class)
					.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
					.bind("globalNames").to(manager.getAttributeModule(GlobalNames.key, unit))
					.instance();
		}
	};


	VarDecl declaration(Variable var);

	PortDecl portDeclaration(Port port);

	EntityDecl entityDeclaration(EntityInstanceExpr instance);

	@Module
	interface Implementation extends Names {
		@Binding
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Binding
		default Map<Port, PortDecl> portDeclarationMap() {
			return new ConcurrentHashMap<>();
		}

		default PortDecl portDeclaration(Port port) {
			return portDeclarationMap().computeIfAbsent(port, this::startPortLookup);
		}

		default PortDecl startPortLookup(Port port) {
			return lookupPort(tree().parent(port), port);
		}

		PortDecl lookupPort(IRNode node, Port port);

		default PortDecl lookupPort(InputPattern input, Port port) {
			return lookupInputPort(tree().parent(input), port);
		}

		default PortDecl lookupPort(OutputExpression output, Port port) {
			return lookupOutputPort(tree().parent(output), port);
		}

		default PortDecl lookupPort(StmtRead read, Port port) {
			return lookupInputPort(tree().parent(read), port);
		}

		default PortDecl lookupPort(StmtWrite write, Port port) {
			return lookupOutputPort(tree().parent(write), port);
		}

		default PortDecl lookupInputPort(IRNode node, Port port) {
			return lookupInputPort(tree().parent(node), port);
		}

		default PortDecl lookupInputPort(Entity entity, Port port) {
			for (PortDecl decl : entity.getInputPorts()) {
				if (decl.getName().equals(port.getName())) {
					return decl;
				}
			}
			return null;
		}

		default PortDecl lookupOutputPort(IRNode node, Port port) {
			return lookupOutputPort(tree().parent(node), port);
		}

		default PortDecl lookupOutputPort(Entity entity, Port port) {
			for (PortDecl decl : entity.getOutputPorts()) {
				if (decl.getName().equals(port.getName())) {
					return decl;
				}
			}
			return null;
		}


		default EntityDecl entityDeclaration(EntityInstanceExpr instance) {
			return lookupEntity(tree().parent(instance), instance.getEntityName());
		}

		default EntityDecl lookupEntity(IRNode node, String name) {
			IRNode parent = tree().parent(node);
			return parent == null ? null : lookupEntity(parent, name);
		}

		default EntityDecl lookupEntity(NamespaceDecl namespaceDecl, String name) {
			return findInStream(namespaceDecl.getEntityDecls().stream(), name)
					.orElseGet(() -> globalNames().entityDecl(namespaceDecl.getQID().concat(QID.of(name)), true));
		}


		@Binding
		default Map<Variable, VarDecl> declarationMap() {
			return new ConcurrentHashMap<>();
		}

		default VarDecl declaration(Variable var) {
			return declarationMap().computeIfAbsent(var, v -> lookup(v, v.getName()));
		}

		default VarDecl lookup(IRNode context, String name) {
			IRNode node = tree().parent(context);
			while (node != null) {
				Optional<VarDecl> d = localLookup(node, context, name);
				if (d.isPresent()) {
					return d.get();
				}
				context = node;
				node = tree().parent(node);
			}
			return null;
		}

		default Optional<VarDecl> localLookup(IRNode node, IRNode context, String name) {
			return Optional.empty();
		}

		default Optional<VarDecl> localLookup(ExprLet let, IRNode context, String name) {
			return findInStream(let.getVarDecls().stream(), name);
		}

		default Optional<VarDecl> localLookup(ExprLambda lambda, IRNode context, String name) {
			return findInStream(lambda.getValueParameters().stream(), name);
		}

		default Optional<VarDecl> localLookup(ExprProc proc, IRNode context, String name) {
			return findInStream(proc.getValueParameters().stream(), name);
		}

		default Optional<VarDecl> localLookup(StmtBlock block, IRNode context, String name) {
			return findInStream(block.getVarDecls().stream(), name);
		}

		default Optional<VarDecl> localLookup(ExprList list, IRNode context, String name) {
			Stream<VarDecl> decls = list.getGenerators().stream()
					.flatMap(generator -> generator.getVariables().stream());
			return findInStream(decls, name);
		}

		default Optional<VarDecl> localLookup(ExprList list, GeneratorFilter context, String name) {
			for (GeneratorFilter g : list.getGenerators()) {
				if (g == context) {
					return Optional.empty();
				}
				for (VarDecl d : g.getVariables()) {
					if (d.getName().equals(name)) {
						return Optional.of(d);
					}
				}
			}
			return Optional.empty();
		}

		Optional<VarDecl> localLookup(GeneratorFilter generator, IRNode node, String name);

		default Optional<VarDecl> localLookup(GeneratorFilter generator, Expression context, String name) {
			if (generator.getCollectionExpr() == context) {
				// The collection expression may only refer to variables declared before this generator.
				return Optional.empty();
			} else if (generator.getFilters().contains(context)) {
				// Filters may refer to their generator variables.
				for (VarDecl d : generator.getVariables()) {
					if (d.getName().equals(name)) {
						return Optional.of(d);
					}
				}
			}
			throw new Error();
		}

		default Optional<VarDecl> localLookup(GeneratorFilter generator, VarDecl context, String name) {
			return Optional.empty();
		}

		default Optional<VarDecl> localLookup(StmtForeach foreach, IRNode context, String name) {
			Stream<VarDecl> decls = foreach.getGenerators().stream()
					.flatMap(generator -> generator.getVariables().stream());
			return findInStream(decls, name);
		}

		default Optional<VarDecl> localLookup(StmtForeach foreach, GeneratorFilter context, String name) {
			for (GeneratorFilter g : foreach.getGenerators()) {
				if (g == context) {
					return Optional.empty();
				}
				for (VarDecl d : g.getVariables()) {
					if (d.getName().equals(name)) {
						return Optional.of(d);
					}
				}
			}
			return Optional.empty();
		}

		default Optional<VarDecl> localLookup(Action action, IRNode context, String name) {
			Stream<VarDecl> actionVars = action.getVarDecls().stream();
			Stream<VarDecl> inputVars = action.getInputPatterns().stream()
					.flatMap(inputPattern -> inputPattern.getVariables().stream());

			return findInStream(Stream.concat(actionVars, inputVars), name);
		}

		default Optional<VarDecl> localLookup(CalActor actor, IRNode context, String name) {
			return findInStream(Stream.concat(actor.getVarDecls().stream(), actor.getValueParameters().stream()), name);
		}

		default Optional<VarDecl> localLookup(NlNetwork network, IRNode context, String name) {
			return findInStream(Stream.concat(network.getVarDecls().stream(), network.getValueParameters().stream()), name);
		}

		default Optional<VarDecl> localLookup(NamespaceDecl ns, IRNode context, String name) {
			Optional<VarDecl> result = findInStream(ns.getVarDecls().stream(), name);
			if (result.isPresent()) {
				return result;
			} else {
				return Optional.ofNullable(globalNames().varDecl(ns.getQID().concat(QID.of(name)), true));
			}
		}

		default <D extends Decl> Optional<D> findInStream(Stream<D> decls, String name) {
			return decls.filter(decl -> decl.getName().equals(name)).findAny();
		}
	}
}