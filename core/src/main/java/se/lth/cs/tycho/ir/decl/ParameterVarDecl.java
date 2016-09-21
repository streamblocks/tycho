package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class ParameterVarDecl extends VarDecl {
	private final Expression defaultValue;

	public ParameterVarDecl(TypeExpr type, String name, Expression defaultValue) {
		this(null, type, name, defaultValue);
	}
	private ParameterVarDecl(VarDecl original, TypeExpr type, String name, Expression defaultValue) {
		super(original, type, name, true, null);
		this.defaultValue = defaultValue;
	}

	public ParameterVarDecl copy(TypeExpr type, String name, Expression defaultValue) {
		if (getType() == type && Objects.equals(getName(), name) && getDefaultValue() == defaultValue) {
			return this;
		} else {
			return new ParameterVarDecl(this, type, name, defaultValue);
		}
	}

	public Expression getDefaultValue() {
		return defaultValue;
	}

	public ParameterVarDecl withDefaultValue(Expression defaultValue) {
		return copy(getType(), getName(), defaultValue);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (getType() != null) action.accept(getType());
		if (getValue() != null) action.accept(getValue());
	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return copy(
				getType() == null ? null : transformation.applyChecked(TypeExpr.class, getType()),
				getName(),
				getDefaultValue() == null ? null : transformation.applyChecked(Expression.class, defaultValue));
	}

	@Override
	public VarDecl withType(TypeExpr type) {
		return copy(type, getName(), defaultValue);
	}

	@Override
	public VarDecl withValue(Expression value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public VarDecl withName(String name) {
		return copy(getType(), name, defaultValue);
	}
}
