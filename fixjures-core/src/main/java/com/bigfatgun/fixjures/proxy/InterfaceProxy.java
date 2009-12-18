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

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Simple interface getter proxy using {@code java.lang.reflect.Proxy}. This will only proxy methods that take no
 * arguments.
 *
 * @author Steve Reed
 */
final class InterfaceProxy<T> extends AbstractObjectProxy<T> implements InvocationHandler {

	private static final class HashCodeSupplier implements Supplier<Integer> {

		private final InterfaceProxy<?> proxy;

		private HashCodeSupplier(final InterfaceProxy<?> proxy) {
			this.proxy = proxy;
		}

		public Integer get() {
			return ImmutableList.copyOf(Iterables.transform(proxy.getStubs().values(), new Function<Supplier<?>, Object>() {
				public Object apply(@Nullable final Supplier<?> valueProvider) {
					if (valueProvider == HashCodeSupplier.this || valueProvider instanceof ToStringSupplier) {
						return 0;
					} else {
						return valueProvider.get();
					}
				}
			})).hashCode();
		}
	}

	private static final class ToStringSupplier implements Supplier<String> {

		private final InterfaceProxy<?> proxy;

		private ToStringSupplier(final InterfaceProxy<?> proxy) {
			this.proxy = proxy;
		}

		public String get() {
			return String.format("Proxy of %s; %s", proxy.getType(), proxy.getStubs().values());
		}
	}

	InterfaceProxy(final Class<T> cls, final ImmutableSet<Fixjure.Option> options) {
		super(cls, options);
		if (!cls.isInterface()) {
			throw new RuntimeException(String.format("Class %s is not an interface.", cls.getName()));
		}

		addValueStub("hashCode", new HashCodeSupplier(this));
		addValueStub("toString", new ToStringSupplier(this));
	}

	/**
	 * Creates a new {@code Proxy} instance with {@code this} as the {@code InvocationHandler}.
	 *
	 * @return new proxy instance
	 */
	public T create() {
		return getType().cast(Proxy.newProxyInstance(getType().getClassLoader(), new Class[]{getType()}, this));
	}

	public Object invoke(final Object object, final Method method, final Object[] parameters) throws Throwable {
		if (parameters != null && parameters.length == 1 && method.getName().equals("equals")) {
			return object == parameters[0];
		}

		if (parameters != null) {
			// TODO : make option for this exception
			throw new RuntimeException("Proxied methods shall take no arguments. Call: " + callToString(method, parameters));
		}

		final ImmutableMap<String, Supplier<?>> stubs = getStubs();

		if (stubs.containsKey(method.getName())) {
			return stubs.get(method.getName()).get();
		} else if (isOptionEnabled(Fixjure.Option.NULL_ON_UNMAPPED)) {
			return null;
		} else {
			throw new FixtureException("Method has not been stubbed. Call: " + callToString(method, parameters));
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
		return String.format("%s.%s(%s)", getType().getName(), method.getName(), (objects == null) ? "" : Lists.newArrayList(objects));
	}
}
