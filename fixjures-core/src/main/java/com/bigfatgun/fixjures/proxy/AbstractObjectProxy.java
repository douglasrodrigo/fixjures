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

import java.util.Map;

import com.bigfatgun.fixjures.ValueProvider;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Base proxy class that holds on to stubs and the object type.
 *
 * @param <T> proxy object type
 * @author Steve Reed
 */
abstract class AbstractObjectProxy<T> implements ObjectProxy<T> {

	/** Proxy object type. */
	private final Class<T> clazz;

	/** Map of method name to stub. */
	private final Map<String, ValueProvider<?>> stubs;

	/**
	 * Constructor required to be invoked by subclasses in order to set up the class and stubs.
	 *
	 * @param cls proxy object type
	 * @throws NullPointerException if {@code cls} is null
	 */
	protected AbstractObjectProxy(final Class<T> cls) {
		clazz = checkNotNull(cls);
		stubs = Maps.newHashMap();
	}

	/**
	 * Returns the proxy object type.
	 * @return the proxy object type
	 */
	public final Class<T> getType() {
		return clazz;
	}

	/**
	 * Returns an immutable map of getter name to value stub.
	 * @return an immutable map of getter name to value stub
	 */
	protected final ImmutableMap<String, ValueProvider<?>> getStubs() {
		return ImmutableMap.copyOf(stubs);
	}

	/**
	 * Adds a value stub for a method with the given name.
	 *
	 * @param methodName method name
	 * @param valueStub method return value stub
	 */
	public final void addValueStub(final String methodName, final ValueProvider<?> valueStub) {
		stubs.put(methodName, valueStub);
	}
}
