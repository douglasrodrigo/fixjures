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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * "Proxies" concrete types by using reflection to instantiate the object
 * and invoke property setters.
 *
 * @param <T> proxy object type
 * @author Steve Reed
 */
public final class ConcreteReflectionProxy<T> extends AbstractObjectProxy<T> {

	/**
	 * Creates a new proxy of the given type.
	 *
	 * @param cls proxy object type
	 * @throws NullPointerException if {@code cls} is null
	 * @throws RuntimeException if {@code cls} is an interface
	 */
	public ConcreteReflectionProxy(final Class<T> cls) {
		super(cls);
		if (cls.isInterface()) {
			throw new RuntimeException(String.format("Class %s is an interface.", cls.getName()));
		}
	}

	/**
	 * Invokes the object's no-arg constructor and invokes setters for all of the known stubs. Returns null if
	 * there is any sort of error while invoking said constructor.
	 * <p>
	 * {@inheritDoc}
	 */
	public T create() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		final T object;
//		try {
			final Constructor<T> ctor = getType().getDeclaredConstructor();
			ctor.setAccessible(true);
			object = ctor.newInstance();
//		} catch (Exception e) {
//			Fixjure.LOGGER.severe(String.format("Error instantiating object of type: %s", getType()));
//			return null;
//		}

		for (final Map.Entry<String, ValueStub> entry : getStubs().entrySet()) {
			setInstanceValue(object, entry.getKey(), convertNameToSetter(entry.getKey()), entry.getValue().invoke());
		}

		return object;
	}

	/**
	 * Uses reflection to find the object's setter method, and invokes it with the given value.
	 *
	 * @param object object to invoke setter on
	 * @param getterName name of getter method
	 * @param setterName name of setter method
	 * @param value value to set
	 */
	private void setInstanceValue(final T object, final String getterName, final String setterName, final Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//		try {
			final Method getter = getType().getMethod(getterName);
			final Method setter = getType().getMethod(setterName, getter.getReturnType());

			setter.invoke(object, value);
//		} catch (NoSuchMethodException e) {
//			Fixjure.LOGGER.warning(String.format("No getter and setter (%s and %s) found in %s", getterName, setterName, getType()));
//		} catch (Exception e) {
//			Fixjure.LOGGER.warning(String.format("Exception while attempting %s in %s", setterName, getType()));
//		}
	}

	/**
	 * Converts getter name to setter name by replacing the first "g" with an "s".
	 *
	 * @param getterName getter name
	 * @return setter name
	 */
	private String convertNameToSetter(final String getterName) {
		return new StringBuilder("s").append(getterName.substring(1)).toString();
	}
}
