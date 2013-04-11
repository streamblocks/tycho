package net.opendf.ir.common;

/**
 * An LValue for assigning to a field of a structure.
 */
public class LValueField extends LValue {
	private LValue structure;
	private Field field;

	/**
	 * Constructs an LValueVield with a structure and a field.
	 * 
	 * @param structure
	 *            the structure
	 * @param field
	 *            the field of the structure
	 */
	public LValueField(LValue structure, Field field) {
		this.structure = structure;
		this.field = field;
	}

	/**
	 * Returns the enclosing structure, e.g. the record.
	 * 
	 * @return the structure
	 */
	public LValue getStructure() {
		return structure;
	}

	/**
	 * Returns the field.
	 * 
	 * @return the field.
	 */
	public Field getField() {
		return field;
	}

	@Override
	public <R, P> R accept(LValueVisitor<R, P> visitor, P parameter) {
		return visitor.visitLValueField(this, parameter);
	}

}
