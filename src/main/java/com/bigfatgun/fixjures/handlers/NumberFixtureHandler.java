package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.FixtureHandler;

/**
 * Handles {@code Number}s.
 */
public final class NumberFixtureHandler implements FixtureHandler<Number, Number> {

	/**
	 * Returns {@code Number.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<Number> getType() {
		return Number.class;
	}

	/**
	 * Converts the given number to the desired type.
	 * <p>
	 * {@inheritDoc}
	 */
	public Number deserialize(final Class desiredType, final Number rawValue, final String name) {
		if (Byte.class.isAssignableFrom(desiredType) || Byte.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.byteValue();
		} else if (Short.class.isAssignableFrom(desiredType) || Short.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.shortValue();
		} else if (Integer.class.isAssignableFrom(desiredType) || Integer.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.intValue();
		} else if (Long.class.isAssignableFrom(desiredType) || Long.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.longValue();
		} else if (Float.class.isAssignableFrom(desiredType) || Float.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.floatValue();
		} else {
			return rawValue.doubleValue();
		}
	}
}
