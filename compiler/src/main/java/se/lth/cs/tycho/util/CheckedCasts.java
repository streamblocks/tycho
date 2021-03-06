package se.lth.cs.tycho.util;

import java.util.OptionalInt;
import java.util.OptionalLong;

public final class CheckedCasts {
	public static OptionalInt toOptInt(OptionalLong value) {
		return value.isPresent() ? OptionalInt.of(toInt(value.getAsLong())) : OptionalInt.empty();
	}
	public static int toInt(long value) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw outOfRange();
		} else {
			return (int) value;
		}
	}

	private static AssertionError outOfRange() {
		return new AssertionError("Value is out of range");
	}
}
