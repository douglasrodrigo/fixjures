package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.FixtureHandler;

/**
 * Handles booleans.
 */
public final class BooleanFixtureHandler implements FixtureHandler<Object, Boolean> {

	/**
	 * Returns {@code Boolean.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	/**
	 * Converts the given object to a boolean. TODO add support for converting a number to a boolean, or perhaps
	 * testing for nulls/non-nulls.
	 * <p>
	 * {@inheritDoc}
	 */
	public Boolean deserialize(final Class desiredType, final Object rawValue, final String name) {
		if (desiredType.isAssignableFrom(rawValue.getClass())) {
			//noinspection unchecked
			return (Boolean) rawValue;
		} else {
			com.bigfatgun.fixjures.Fixjure.warn("Type mismatch, stubbing " + name + " to return null.");
			return null;
		}
	}
}
