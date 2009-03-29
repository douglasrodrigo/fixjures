package com.bigfatgun.fixjures.mock;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Stub;

/**
 * Exposes the helper methods of MockObjectTestCase as static methods for
 * brevity.
 *
 * @author Steve Reed
 */
public final class MockHelper {

	/** The singleton utility. */
	private static final MockHelper INSTANCE = new MockHelper();

	/** wrapped mock helper. */
	private final MockObjectTestCase helper;

	/**
	 * Utility constructor which creates a new {@code MockObjectTestCase}.
	 */
	private MockHelper() {
		this.helper = new MockObjectTestCase() {
			// nothing
		};
	}

	/**
	 * @param object object to return
	 * @return new return value stub
	 */
	public static Stub returnValue(final Object object) {
		return INSTANCE.helper.returnValue(object);
	}

	/**
	 * Creates a mock object for the given class.
	 *
	 * @param clazz the class to mock
	 * @return a mock object for the given class
	 */
	public static Mock mock(final Class clazz) {
		return INSTANCE.helper.mock(clazz);
	}
}
