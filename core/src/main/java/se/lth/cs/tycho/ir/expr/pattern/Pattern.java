package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

public abstract class Pattern extends AbstractIRNode {
	public Pattern(IRNode original) {
		super(original);
	}

	@Override
	public IRNode clone() {
		return super.clone();
	}

	@Override
	public IRNode deepClone() {
		return super.deepClone();
	}
}
