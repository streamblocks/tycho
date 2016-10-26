package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class NlNetwork extends Entity {
	private final ImmutableList<TypeDecl> typeDecls;
	private final ImmutableList<LocalVarDecl> varDecls;
	private final ImmutableList<InstanceDecl> entities;
	private final ImmutableList<StructureStatement> structure;

	public NlNetwork(List<TypeDecl> typePars,
					 List<ParameterVarDecl> valuePars, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls,
					 List<PortDecl> inputPorts, List<PortDecl> outputPorts,
					 List<InstanceDecl> entities, List<StructureStatement> structure) {
		this(null, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts, entities, structure);
	}

	private NlNetwork(NlNetwork original,
			List<TypeDecl> typePars, List<ParameterVarDecl> valuePars,
			List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls, List<PortDecl> inputPorts,
			List<PortDecl> outputPorts, List<InstanceDecl> entities,
			List<StructureStatement> structure) {

		super(original, inputPorts, outputPorts, typePars, valuePars);

		this.typeDecls = ImmutableList.from(typeDecls);
		this.varDecls = ImmutableList.from(varDecls);
		this.entities = ImmutableList.from(entities);
		this.structure = ImmutableList.from(structure);
	}

	public NlNetwork copy(List<TypeDecl> typePars,
			List<ParameterVarDecl> valuePars, List<TypeDecl> typeDecls, List<LocalVarDecl> varDecls,
			List<PortDecl> inputPorts, List<PortDecl> outputPorts,
			List<InstanceDecl> entities, List<StructureStatement> structure) {
		if (Lists.sameElements(this.typeParameters, typePars)
				&& Lists.sameElements(this.valueParameters, valuePars)
				&& Lists.sameElements(this.typeDecls, typeDecls)
				&& Lists.sameElements(this.varDecls, varDecls)
				&& Lists.sameElements(this.inputPorts, inputPorts)
				&& Lists.sameElements(this.outputPorts, outputPorts)
				&& Lists.sameElements(this.entities, entities)
				&& Lists.sameElements(this.structure, structure)) {
			return this;
		}
		return new NlNetwork(this, typePars, valuePars, typeDecls, varDecls, inputPorts, outputPorts,
				entities, structure);
	}

	@Override
	public <R, P> R accept(EntityVisitor<R, P> visitor, P param) {
		return visitor.visitNlNetwork(this, param);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NlNetwork transformChildren(Transformation transformation) {
		return copy(
				(ImmutableList) typeParameters.map(transformation),
				(ImmutableList) valueParameters.map(transformation),
				(ImmutableList) typeDecls.map(transformation),
				(ImmutableList) varDecls.map(transformation),
				(ImmutableList) inputPorts.map(transformation),
				(ImmutableList) outputPorts.map(transformation),
				(ImmutableList) entities.map(transformation),
				(ImmutableList) structure.map(transformation)
		);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		typeParameters.forEach(action);
		valueParameters.forEach(action);
		typeDecls.forEach(action);
		varDecls.forEach(action);
		inputPorts.forEach(action);
		outputPorts.forEach(action);
		entities.forEach(action);
		structure.forEach(action);
	}

	public ImmutableList<LocalVarDecl> getVarDecls() {
		return varDecls;
	}

	public NlNetwork withVarDecls(List<LocalVarDecl> varDecls) {
		if (Lists.sameElements(this.varDecls, varDecls)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure);
		}
	}
	public NlNetwork withValueParameters(List<ParameterVarDecl> valueParameters) {
		if (Lists.sameElements(this.valueParameters, valueParameters)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure);
		}
	}

	public ImmutableList<InstanceDecl> getEntities() {
		return entities;
	}

	public NlNetwork withEntities(List<InstanceDecl> entities) {
		if (Lists.sameElements(this.entities, entities)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure);
		}
	}

	public ImmutableList<StructureStatement> getStructure() {
		return structure;
	}

	public NlNetwork withStructure(List<StructureStatement> structure) {
		if (Lists.sameElements(this.structure, structure)) {
			return this;
		} else {
			return new NlNetwork(this, typeParameters, valueParameters, typeDecls, varDecls, inputPorts, outputPorts, entities, structure);
		}
	}
}
