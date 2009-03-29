package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.FixtureHandler;

/**
 * Handles strings.
 */
public final class StringFixtureHandler implements FixtureHandler<Object, String> {

	/**
	 * Returns {@code String.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<String> getType() {
		return String.class;
	}

	/**
	 * Converts the object to a string only if it's assignable from the {@code desiredType}.
	 * <p>
	 * {@inheritDoc}
	 */
	public String deserialize(final Class desiredType, final Object rawValue, final String name) {
		if (desiredType.isAssignableFrom(rawValue.getClass())) {
			//noinspection unchecked
			return String.valueOf(rawValue);
		} else {
			com.bigfatgun.fixjures.Fixjure.warn("Type mismatch, stubbing " + name + " to return null.");
			return null;
		}
	}
}
