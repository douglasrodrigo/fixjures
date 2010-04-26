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

package com.bigfatgun.fixjures;

import com.bigfatgun.fixjures.serializable.ObjectInputStreamSource;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class FixjureTest {

	@Test
	public void constructorIsPrivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Constructor<Fixjure> ctor = Fixjure.class.getDeclaredConstructor();
		assertFalse(ctor.isAccessible());
		ctor.setAccessible(true);
		assertNotNull(ctor.newInstance());
	}

	@Test
	public void serializableTest1() throws IOException {
		FixtureFactory fact = FixtureFactory.newObjectInputStreamFactory(Strategies.newResourceStrategy(FixjureTest.class.getClassLoader(), new Strategies.ResourceNameStrategy() {
			public String getResourceName(final Class<?> type, final String name) {
				return "" + name + ".ser";
			}
		}));
		String s = fact.createFixture(String.class, "string1");
		assertEquals("abcdefghijklm\nopqrs\tuvwxyz", s);
	}

	@Test
	public void serializableTest2() throws IOException {
		final List<String> list = new LinkedList<String>();
		Iterables.addAll(list, Fixjure.of(String.class).fromStream(ObjectInputStreamSource.newResource(FixjureTest.class.getClassLoader(), "string2.ser")).createAll());
		assertEquals(2, list.size());
		assertEquals("first", list.get(0));
		assertEquals("second", list.get(1));
	}
}
