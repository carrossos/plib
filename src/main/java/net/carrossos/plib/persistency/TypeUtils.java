package net.carrossos.plib.persistency;

import java.util.HashSet;
import java.util.Set;

public class TypeUtils {

	private static final Set<Class<?>> PRIMITIVES_OR_WRAPPERS = new HashSet<>();

	static {
		PRIMITIVES_OR_WRAPPERS.add(boolean.class);
		PRIMITIVES_OR_WRAPPERS.add(Boolean.class);

		PRIMITIVES_OR_WRAPPERS.add(char.class);
		PRIMITIVES_OR_WRAPPERS.add(Character.class);

		PRIMITIVES_OR_WRAPPERS.add(byte.class);
		PRIMITIVES_OR_WRAPPERS.add(Byte.class);

		PRIMITIVES_OR_WRAPPERS.add(short.class);
		PRIMITIVES_OR_WRAPPERS.add(Short.class);

		PRIMITIVES_OR_WRAPPERS.add(int.class);
		PRIMITIVES_OR_WRAPPERS.add(Integer.class);

		PRIMITIVES_OR_WRAPPERS.add(long.class);
		PRIMITIVES_OR_WRAPPERS.add(Long.class);

		PRIMITIVES_OR_WRAPPERS.add(float.class);
		PRIMITIVES_OR_WRAPPERS.add(Float.class);

		PRIMITIVES_OR_WRAPPERS.add(double.class);
		PRIMITIVES_OR_WRAPPERS.add(Double.class);
	}

	private TypeUtils() {
	}

	public static boolean isPrimitiveOrWrapper(Class<?> type) {
		return PRIMITIVES_OR_WRAPPERS.contains(type);
	}

	public static float toFloat(double value) {
		if (value < Float.MIN_VALUE || value > Float.MAX_VALUE) {
			throw new PersistencyException("Invalid conversion to short for value '" + value + "'");
		} else {
			return (float) value;
		}
	}

	public static int toInt(long value) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new PersistencyException("Invalid conversion to int for value '" + value + "'");
		} else {
			return (int) value;
		}
	}

	public static long toLong(double value) {
		if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
			throw new PersistencyException("Invalid conversion to long for value '" + value + "'");
		} else {
			return (long) value;
		}
	}

	public static short toShort(int value) {
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
			throw new PersistencyException("Invalid conversion to short for value '" + value + "'");
		} else {
			return (short) value;
		}
	}

}
