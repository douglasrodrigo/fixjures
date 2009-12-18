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
package com.bigfatgun.fixjures.proxy;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.Suppliers;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class ConcreteReflectionProxyTest {

	private static interface Foo {

	}

	public static class FooTwo {
		boolean ctorInvoked;

		public FooTwo() {
			ctorInvoked = true;
		}
	}

	@Test(expected = RuntimeException.class)
	public void doNotPassInterface() {
		new ConcreteReflectionProxy<Foo>(Foo.class);
	}

	@Test(expected = FixtureException.class)
	public void bogusGetterName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
		final ConcreteReflectionProxy<FooTwo> proxy = new ConcreteReflectionProxy<FooTwo>(FooTwo.class);
		proxy.addValueStub("bogus", Suppliers.of("dude"));
		FooTwo two = proxy.create();
		assertNotNull(two);
	}
}
