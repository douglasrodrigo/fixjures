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

/**
 * Static proxy factory.
 */
public final class Proxies {

	/**
	 * Creates a proxy object based on the class. If the class is an interface, a
	 * {@link com.bigfatgun.fixjures.proxy.InterfaceProxy} is returned, otherwise a
	 * {@link com.bigfatgun.fixjures.proxy.ConcreteReflectionProxy} is assumed to be the appropriate
	 * proxy.
	 *
	 * @param cls proxy object type
	 * @param <T> proxy object type
	 * @return object proxy
	 */
	public static <T> ObjectProxy<T> newProxy(final Class<T> cls) {
		if (cls.isInterface()) {
			return newInterfaceProxy(cls);
		} else {
			return newJavaBeanProxy(cls);
		}
	}

	/**
	 * Creates a new interface proxy.
	 *
	 * @param cls proxy object type
	 * @param <T> proxy object type
	 * @param options fixjure options
	 * @return new object proxy
	 */
	public static <T> ObjectProxy<T> newInterfaceProxy(final Class<T> cls, final Fixjure.Option... options) {
		final InterfaceProxy<T> proxy = new InterfaceProxy<T>(cls);
		proxy.enableOptions(options);
		return proxy;
	}

	/**
	 * Creates a new concrete java bean proxy.
	 *
	 * @param cls proxy object type
	 * @param <T> proxy object type
	 * @return new object proxy
	 */
	public static <T> ObjectProxy<T> newJavaBeanProxy(final Class<T> cls) {
		return new ConcreteReflectionProxy<T>(cls);
	}

	/**
	 * Private util ctor.
	 */
	private Proxies() {
		assert false : "Cannot instantiate!";
	}
}
