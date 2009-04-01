/*
 * Copyright (C) 2009 Steve Reed
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
package com.bigfatgun.fixjures.proxy;

/**
 * A {@link ValueStub} that simply holds on to an existing object as the value.
 *
 * @author Steve Reed
 */
public final class ValueStubImpl implements ValueStub {

	/** The value. */
	private final Object obj;

	/**
	 * Creates a new value stub.
	 *
	 * @param value the value
	 */
	public ValueStubImpl(final Object value) {
		obj = value;
	}

	/**
	 * @return the value
	 */
	public Object invoke() {
		return obj;
	}
}
