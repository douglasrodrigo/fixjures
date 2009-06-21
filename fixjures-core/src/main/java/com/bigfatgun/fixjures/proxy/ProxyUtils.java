package com.bigfatgun.fixjures.proxy;

public final class ProxyUtils {
	private ProxyUtils() {}

	/**
	 * Converts a property name from JSON into a getter name. For example, "firstName" will be transformed into
	 * "getFirstName".
	 *
	 * @param propertyName property name
	 * @return getter name
	 */
	public static String getterName(final String propertyName) {
		final StringBuilder builder = new StringBuilder("get");
		builder.append(Character.toUpperCase(propertyName.charAt(0)));
		builder.append(propertyName.substring(1));
		return builder.toString();
	}
}
