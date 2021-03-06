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

package com.bigfatgun.fixjures.json;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked"})
public class JSONSourceTest {

	private static interface Foo {

		public String getBar();
	}

	public static class FooTwo {
		private String electric;

		public FooTwo() {
			// yeah
		}

		public String getElectric() {
			return electric;
		}

		public void setElectric(final String electric) {
			this.electric = electric;
		}
	}

	public static class FooThree extends FooTwo {
		private FooThree() {
			super();
			setElectric("slide");
		}
	}

	public static interface Complex {
		String getStr();

		byte getByte();

		Double getDouble();

		List<Byte> getList();

		Set<Byte> getSet();

		Multiset<Short> getMultiset();

		Map getMap();

		Complex getParent();

		String[] getArray();
	}

	@Test
	public void simpleStringLiteralToInterface() throws Exception {
		final Foo foo = Fixjure.of(Foo.class).from(JSONSource.newJsonString("{ \"bar\" : \"" + toString() + "\" }")).create();
		assertNotNull(foo);
		assertEquals(toString(), foo.getBar());
	}

	@Test
	public void simpleStringLiteralToConcrete() throws Exception {
		final FooTwo foo = Fixjure.of(FooTwo.class).from(JSONSource.newJsonString("{ \"electric\" : \"boogaloo\" }")).create();
		assertNotNull(foo);
		assertEquals("boogaloo", foo.getElectric());
	}

	@Test
	public void simpleStringLiteralToConcreteWithPrivateConstructor() throws Exception {
		final FooThree foo = Fixjure.of(FooThree.class).from(JSONSource.newJsonString("{ \"electric\" : \"boogaloo\" }")).create();
		assertNotNull(foo);
		assertEquals("boogaloo", foo.getElectric());
	}

	@Test
	public void complexFromResource() throws Exception {
		final Complex complex = Fixjure.of(Complex.class).from(JSONSource.newJsonResource("dummy1.json")).create();
		assertEquals("Some String!", complex.getStr());
		assertEquals((byte) 4, complex.getByte());
		assertEquals((Double) 1e14, complex.getDouble());
		// failing because type params aren't getting sent through
		assertEquals(Lists.newArrayList((byte) 1, (byte) 2, (byte) 3, (byte) 4), complex.getList());
		assertEquals(Sets.newHashSet((byte) 1, (byte) 2, (byte) 3, (byte) 4), complex.getSet());
		assertEquals(ImmutableMultiset.of((short) 1, (short) 1, (short) 2, (short) 3, (short) 5, (short) 8), complex.getMultiset());
		assertEquals("one", complex.getMap().get("1"));
		assertEquals("two", complex.getMap().get("2"));
		assertNotNull(complex.getParent());
		assertEquals("I'm the parent!", complex.getParent().getStr());
		assertTrue(Arrays.equals(new String[]{"a", "b", "c"}, complex.getArray()));
	}

	@Test
	public void booleansAreSupported() throws Exception {
		assertTrue(Fixjure.of(Boolean.class).from(JSONSource.newJsonString(" true ")).create());
	}

	public static interface WithNonGenericList {
		List getFoo();
	}

	@Test
	public void nonGenericList() throws Exception {
		assertEquals(Arrays.asList(1L, 2L), Fixjure.of(WithNonGenericList.class).from(JSONSource.newJsonString("{ \"foo\" : [ 1, 2 ] }")).create().getFoo());
	}

	@Test
	public void correctUseOfNonGenericList() throws Exception {
		List l = Fixjure.listOf(Integer.class).from(JSONSource.newJsonString("[1, 2]")).create();
		assertEquals(Arrays.asList(1, 2), l);
	}

	@Test(expected = FileNotFoundException.class)
	public void fileNotFoundOnBadFile() throws FileNotFoundException {
		JSONSource.newJsonFile(new File("foo"));
	}

	@Test(expected = RuntimeException.class)
	public void attemptToConvertListToUnknownCollectionType() throws Exception {
		assertNull(Fixjure.of(Complex.class).from(JSONSource.newJsonString("{ str : [1, 2] }")).create().getStr());
	}

	private static class BadCtor {
		public BadCtor() {
			throw new RuntimeException("worst constructor ever.");
		}
	}

	@Test(expected = FixtureException.class)
	public void exceptionOnConcreteCtor() throws Exception {
		assertNull(Fixjure.of(BadCtor.class).from(JSONSource.newJsonString("{ 1: 2 }")).create());
	}

	private static class BadGettersAndSetters {

		public BadGettersAndSetters() {
			// yeah
		}

		private String foo;

		private String getFoo() {
			return foo;
		}

		private String bar;

		public String getBar() {
			return bar;
		}

		public void setBar(String str) {
			throw new RuntimeException();
		}
	}

	@Test(expected = FixtureException.class)
	public void badGettersAndSetters() throws Exception {
		assertNull(Fixjure.of(BadGettersAndSetters.class).from(JSONSource.newJsonString("{ \"unknown\" : 1, \"bar\" : 2, \"foo\" : 3 }")).create());
	}

	@Test
	public void simpleArrayOfNumbers() throws Exception {
		int[] ints = new int[0];
		ints = Fixjure.of(ints.getClass()).from(JSONSource.newJsonString("[ 1, 2, 3 ]")).create();
		assertNotNull(ints.length);
		assertEquals(1, ints[0]);
		assertEquals(2, ints[1]);
		assertEquals(3, ints[2]);
	}

	private static interface HasBools {
		boolean getFirst();

		Boolean getSecond();
	}

	@Test(expected = FixtureException.class)
	public void booleansWork() {
		HasBools hasbools = Fixjure.of(HasBools.class).from(JSONSource.newJsonString("{ first : true, second : false }")).create();
//		assertTrue(hasbools.getFirst());
//		assertFalse(hasbools.getSecond());
//		hasbools = Fixjure.of(HasBools.class).from(new JSONSource("{ first : false, second : true }")).create();
//		assertFalse(hasbools.getFirst());
//		assertTrue(hasbools.getSecond());
	}

	private static interface Decorator<T> {
		T getT();
	}

	@Test
	public void decoratorOfStringWorks() {
		Decorator<String> ds = Fixjure.of(Decorator.class).of(String.class).from(JSONSource.newJsonString("{ \"t\": \"foo\" }")).create();
		assertEquals("foo", ds.getT());
	}

	@Test
	public void mapOfIntToLong() {
		Fixjure.SourcedFixtureBuilder<Map<Integer, Long>> fixtureBuilder = Fixjure.mapOf(Integer.class, Long.class).from(JSONSource.newJsonString("{\"1\":1,\"2\":2,\"3\":10000000000}"));
		Map<Integer, Long> m = fixtureBuilder.create();
		assertNotNull(m);
		assertEquals(3, m.size());
		assertEquals(1L, m.get(1).longValue());
		assertEquals(2L, m.get(2).longValue());
		assertEquals(10000000000L, m.get(3).longValue());
	}
}
