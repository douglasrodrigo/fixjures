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
import com.google.common.collect.ImmutableSet;
import static org.junit.Assert.*;
import org.junit.Test;

public class InterfaceProxyTest {

	private static class Foo {

	}

	private static interface Foo2 {
		String convertThing(Object obj1);

		String getThing();
	}

	@Test(expected = RuntimeException.class)
	public void doNotPassConcreteClass() {
		new InterfaceProxy<Foo>(Foo.class, ImmutableSet.<Fixjure.Option>of());
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeMethodWithArgs() {
		new InterfaceProxy<Foo2>(Foo2.class, ImmutableSet.<Fixjure.Option>of()).get().convertThing(this);
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeUnstubbedMethod() {
		new InterfaceProxy<Foo2>(Foo2.class, ImmutableSet.<Fixjure.Option>of()).get().getThing();
	}

	private static interface Foo3 {
		String getStr();
	}

	@Test
	public void proxiesUseIdentityForEquals() {
		final Foo3 foo1 = new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).get();
		final Foo3 foo2 = new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).get();
		assertEquals(foo1, foo1);
		assertFalse(foo1.equals(foo2));
	}

	@Test
	public void proxiesHaveToStr() {
		assertTrue(new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).get().toString().startsWith("Proxy of " + Foo3.class));
	}
}
