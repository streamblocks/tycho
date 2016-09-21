package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class LocalVarDecl extends VarDecl {
	public LocalVarDecl(TypeExpr type, String name, boolean constant, Expression value) {
		this(null, type, name, constant, value);
	}
	private LocalVarDecl(LocalVarDecl original, TypeExpr type, String name, boolean constant, Expression value) {
		super(original, type, name, constant, value);
	}

	public LocalVarDecl copy(TypeExpr type, String name, boolean constant, Expression value) {
		if (getType() == type && Objects.equals(getName(), name) && isConstant() == constant && getValue() == value) {
			return this;
		} else {
			return new LocalVarDecl(this, type, name, constant, value);
		}
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
				isConstant(),
				getValue() == null ? null : transformation.applyChecked(Expression.class, getValue()));
	}

	@Override
	public VarDecl withType(TypeExpr type) {
		return copy(type, getName(), isConstant(), getValue());
	}

	@Override
	public VarDecl withValue(Expression value) {
		return copy(getType(), getName(), isConstant(), value);
	}

	@Override
	public VarDecl withName(String name) {
		return copy(getType(), name, isConstant(), getValue());
	}

	@Override
	public LocalVarDecl clone() {
		return (LocalVarDecl) super.clone();
	}

	@Override
	public LocalVarDecl deepClone() {
		return (LocalVarDecl) super.deepClone();
	}
}
