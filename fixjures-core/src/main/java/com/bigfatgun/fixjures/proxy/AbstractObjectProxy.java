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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.bigfatgun.fixjures.Fixjure;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.FixtureTypeDefinition;
import com.bigfatgun.fixjures.ValueProvider;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base proxy class that holds on to stubs and the object type.
 *
 * @author Steve Reed
 * @param <T> proxy object type
 */
abstract class AbstractObjectProxy<T> implements ObjectProxy<T> {

	private final Class<T> clazz;
	private final Map<String, ValueProvider<?>> stubs;
	private final Set<Fixjure.Option> options;

	protected AbstractObjectProxy(final Class<T> cls, final Set<Fixjure.Option> options) {
		this.clazz = checkNotNull(cls);
		this.stubs = Maps.newHashMap();
		this.options = Sets.newEnumSet(options, Fixjure.Option.class);
	}

	public final Class<T> getType() {
		return clazz;
	}

	public void enableOptions(final Fixjure.Option[] options) {
		this.options.addAll(Arrays.asList(options));
	}

	@Override
	public final FixtureTypeDefinition suggestType(final String key) {
		final Method getter;
		try {
			getter = getType().getMethod(key);
			return FixtureTypeDefinition.wrapMethodReturnType(getter);
		} catch (NoSuchMethodException e) {
			if (isOptionEnabled(Fixjure.Option.SKIP_UNMAPPABLE)) {
				return null;
			} else {
				throw convert(e);
			}
		}
	}

	protected boolean isOptionEnabled(final Fixjure.Option option) {
		return options.contains(option);
	}

	/**
	 * Returns an immutable map of getter name to value stub.
	 *
	 * @return an immutable map of getter name to value stub
	 */
	protected final ImmutableMap<String, ValueProvider<?>> getStubs() {
		return ImmutableMap.copyOf(stubs);
	}

	/**
	 * Adds a value stub for a method with the given name.
	 *
	 * @param methodName method name
	 * @param valueStub  method return value stub
	 */
	public final void addValueStub(final String methodName, final ValueProvider<?> valueStub) {
		stubs.put(methodName, valueStub);
	}
}
