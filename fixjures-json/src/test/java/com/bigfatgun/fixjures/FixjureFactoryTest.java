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

import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;


public class FixjureFactoryTest {

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceFactory() {
		FixtureFactory.newFactory(null);
	}

	@Test(expected = NullPointerException.class) @Ignore
	public void nullPointerExceptionWithNullSourceStrategyForJsonFactory() {
//		FixtureFactory.newJsonFactory(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceStrategyForObjInStreamFactory() {
		FixtureFactory.newObjectInputStreamFactory(null);
	}

	@Test(expected = NullPointerException.class) @Ignore
	public void nullPointerFromCreateFixtureWithNullType() {
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.of();
//		FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(null, "non-null");
	}

	@Test(expected = NullPointerException.class) @Ignore
	public void nullPointerFromCreateFixtureWithNullName() {
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.of();
//		FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(String.class, null);
	}

	@Test(expected = FixtureException.class) @Ignore
	public void unknownObjectName() {
		final Map<String, String> objs = ImmutableMap.of("good", "ugly");
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(String.class, objs);
//		FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(String.class, "bad");
	}

	@Test(expected = FixtureException.class) @Ignore
	public void unknownObjectType() {
		final Map<String, String> objs = ImmutableMap.of("good", "ugly");
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(String.class, objs);
//		FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(Integer.class, "good");
	}

	private static interface Foo {
		String getName();
	}

	@Test @Ignore
	public void enableOption() {
		final Map<String, String> foos = ImmutableMap.of("only", "{\"name\":\"Foo Bar\", \"values\":\"Does Not Map\"}");
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(Foo.class, foos);
//		final FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).enableOption(Fixjure.Option.SKIP_UNMAPPABLE);
//		final Foo foo = fact.createFixture(Foo.class, "only");
//		assertNotNull(foo);
//		assertEquals("Foo Bar", foo.getName());
	}

	@Test(expected = FixtureException.class) @Ignore
	public void disableOption() {
		final Map<String, String> foos = ImmutableMap.of("only", "{name:'Foo Bar', value:'Does Not Map'}");
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(Foo.class, foos);
//		final FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).enableOption(Fixjure.Option.SKIP_UNMAPPABLE).disableOption(Fixjure.Option.SKIP_UNMAPPABLE);
//		fact.createFixture(Foo.class, "only");
	}

	private static interface FooChild extends Foo {
		Foo getParent();
	}

	@Test @Ignore
	public void identityResolution() {
		final Map<String, String> foos = ImmutableMap.of(
				"parent", "{\"name\":\"Foo Bar\"}"
		);
		final Map<String, String> foochilds = ImmutableMap.of(
				"child", "{\"name\":\"Foo Bar Jr.\", \"parent\":\"parent\"}"
		);
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(
				Foo.class, foos,
				FooChild.class, foochilds
		);
//		final FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem));
//		final FooChild fooChild = fact.createFixture(FooChild.class, "child");
//		assertNotNull(fooChild);
//		assertNotNull(fooChild.getParent());
//		assertEquals("Foo Bar", fooChild.getParent().getName());
	}

	@Test(expected = FixtureException.class) @Ignore
	public void identityResolutionBadId() {
		final Map<String, String> foos = ImmutableMap.of(
				"parent", "{name:'Foo Bar'}"
		);
		final Map<String, String> foochilds = ImmutableMap.of(
				"ok", "{name:'I am ok'}",
				"child", "{name:'Foo Bar Jr.', parent:'parent_'}"
		);
		final Map<Class<?>, Map<String, String>> mem = ImmutableMap.<Class<?>, Map<String, String>>of(
				Foo.class, foos,
				FooChild.class, foochilds
		);
//		final FixtureFactory factory = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem));
//		assertEquals("I am ok", factory.createFixture(FooChild.class, "ok").getName());
//		final FooChild child = factory.createFixture(FooChild.class, "child");
//		assertNotNull(child);
//		child.getParent();
	}
}
