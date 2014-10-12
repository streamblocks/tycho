package se.lth.cs.tycho.ir.entity.xdf;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;

public class XDFInstance extends AbstractIRNode {
	private final String name;
	private final QID entity;

	public XDFInstance(String name, QID entity) {
		this(null, name, entity);
	}

	public XDFInstance(IRNode original, String name, QID entity) {
		super(original);
		this.name = name;
		this.entity = entity;
	}

	public String getName() {
		return name;
	}

	public QID getEntity() {
		return entity;
	}

}