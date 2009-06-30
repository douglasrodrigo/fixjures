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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.annotation.Nullable;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Simple interface getter proxy using {@code java.lang.reflect.Proxy}. This will only proxy methods that
 * take no arguments.
 *
 * @param <T> proxy object type
 * @author Steve Reed
 */
final class InterfaceProxy<T> extends AbstractObjectProxy<T> implements InvocationHandler {

	/**
	 * Creates a new proxy of the given class.
	 *
	 * @param cls class to proxy, must be an interface
	 * @throws NullPointerException if {@code cls} is null
	 * @throws RuntimeException if {@code cls} is not an interface
	 */
	InterfaceProxy(final Class<T> cls, final ImmutableSet<Fixjure.Option> options) {
		super(cls, options);
		if (!cls.isInterface()) {
			throw new RuntimeException(String.format("Class %s is not an interface.", cls.getName()));
		}
	}

	/**
	 * Creates a new {@code Proxy} instance with {@code this} as the {@code InvocationHandler}.
	 *
	 * @return new proxy instance
	 */
	public T create() {
		return getType().cast(Proxy.newProxyInstance(getType().getClassLoader(), new Class[] { getType() }, this));
	}

	/**
	 * {@inheritDoc}
	 * @throws RuntimeException if a method with arguments is invoked, that's not supported yet, or if a method that
	 * 	hasn't been stubbed is invoked
	 */
	public Object invoke(final Object object, final Method method, final Object[] objects) throws Throwable {
		if (objects != null && objects.length == 1 && method.getName().equals("equals")) {
			return object == objects[0];
		}

		if (objects != null) {
			throw new RuntimeException("Proxied methods shall take no arguments. Call: " + callToString(method, objects));
		}

		if (method.getName().equals("hashCode")) {
			return Iterables.transform(getStubs().values(), new Function<Supplier<?>, Object>() {
				@Override
				public Object apply(@Nullable final Supplier<?> valueProvider) {
					return valueProvider.get();
				}
			}).hashCode();
		} else if (method.getName().equals("toString")) {
			return "Proxy of " + getType() + "; " + getStubs().values();
		}

		if (getStubs().containsKey(method.getName())) {
			return getStubs().get(method.getName()).get();
		} else if (isOptionEnabled(Fixjure.Option.NULL_ON_UNMAPPED)) {
			return null;
		} else {
			throw new FixtureException("Method has not been stubbed. Call: " + callToString(method, objects));
		}
	}

	/**
	 * Stringifies a method invocation in the form "Class.method([arg1, arg2, arg3])".
	 *
	 * @param method method being invoked
	 * @param objects method arguments
	 * @return stringified method call
	 */
	private String callToString(final Method method, final Object... objects) {
		return String.format("%s.%s(%s)", getType().getName(), method.getName(), (objects == null ) ? "" : Lists.newArrayList(objects));
	}
}
