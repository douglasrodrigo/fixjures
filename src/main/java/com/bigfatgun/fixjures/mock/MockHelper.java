/*
 * Copyright (C) 2009 bigfatgun.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
