/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigfatgun.fixjures.proxy;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * "Proxies" concrete types by using reflection to instantiate the object and invoke property setters.
 *
 * @author Steve Reed
 */
final class ConcreteReflectionProxy<T> extends AbstractObjectProxy<T> {

	ConcreteReflectionProxy(final Class<T> cls, final ImmutableSet<Fixjure.Option> options) {
		super(cls, options);
		if (cls.isInterface()) {
			throw new IllegalStateException(String.format("Class %s is an interface.", cls.getName()));
		}
	}

	/**
	 * Invokes the object's no-arg constructor and invokes setters for all of the known stubs. Returns null if there is any
	 * sort of error while invoking said constructor.
	 * <p/>
	 * {@inheritDoc}
	 */
	@Override
	public T get() {
		try {
			final Constructor<T> ctor = getType().getDeclaredConstructor();
			ctor.setAccessible(true);
			final T object = ctor.newInstance();

			for (final Map.Entry<String, Supplier<?>> entry : getStubs().entrySet()) {
                String setter = ProxyUtils.convertNameToSetter(entry.getKey());
                if (setter == null && !this.isOptionEnabled(Fixjure.Option.SKIP_UNMAPPABLE)) {
                    throw new FixtureException("Cannot find setter for " + entry.getKey());
                }
                setInstanceValue(object, entry.getKey(), setter, entry.getValue().get());
			}

			return object;
		} catch (Exception e) {
			throw FixtureException.convert(e);
		}
	}

	/**
	 * Uses reflection to find the object's setter method, and invokes it with the given value.
	 *
	 * @param object object to invoke setter on
	 * @param getterName name of getter method
	 * @param setterName name of setter method
	 * @param value value to set
	 * @throws IllegalAccessException if getter or setter cannot be accessed
	 * @throws java.lang.reflect.InvocationTargetException if setter cannot be invoked
	 */
	private void setInstanceValue(final T object, final String getterName, final String setterName, final Object value) throws InvocationTargetException, IllegalAccessException {
        try {
            final Method getter = getType().getMethod(getterName);
            final Method setter = getType().getMethod(setterName, getter.getReturnType());
            setter.invoke(object, value);
        } catch (NoSuchMethodException e) {
            if (!this.isOptionEnabled(Fixjure.Option.SKIP_UNMAPPABLE)) {
                throw FixtureException.convert(e);
            }
        }
    }
}
