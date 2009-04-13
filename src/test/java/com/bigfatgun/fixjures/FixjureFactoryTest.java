package com.bigfatgun.fixjures;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class FixjureFactoryTest {

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceFactory() {
		FixjureFactory.newFactory(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceStrategyForJsonFactory() {
		FixjureFactory.newJsonFactory(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceStrategyForObjInStreamFactory() {
		FixjureFactory.newObjectInputStreamFactory(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerFromCreateFixtureWithNullType() {
		final Map<Class<?>, Map<String,byte[]>> mem = ImmutableMap.of();
		FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(null, "non-null");
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerFromCreateFixtureWithNullName() {
		final Map<Class<?>, Map<String,byte[]>> mem = ImmutableMap.of();
		FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(String.class, null);
	}

	@Test(expected = FixtureException.class)
	public void unknownObjectName() {
		final Map<String,byte[]> objs = ImmutableMap.of("good", "ugly".getBytes());
		final Map<Class<?>, Map<String,byte[]>> mem = ImmutableMap.<Class<?>, Map<String,byte[]>>of(String.class, objs);
		FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(String.class, "bad");
	}

	@Test(expected = FixtureException.class)
	public void unknownObjectType() {
		final Map<String,byte[]> objs = ImmutableMap.of("good", "ugly".getBytes());
		final Map<Class<?>, Map<String,byte[]>> mem = ImmutableMap.<Class<?>, Map<String,byte[]>>of(String.class, objs);
		FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).createFixture(Integer.class, "good");
	}

	private static interface Foo {
		String getName();
	}

	@Test
	public void enableOption() {
		final Map<String, byte[]> foos = ImmutableMap.of("only", "{name:'Foo Bar', value:'Does Not Map'}".getBytes());
		final Map<Class<?>, Map<String, byte[]>> mem = ImmutableMap.<Class<?>, Map<String,byte[]>>of(Foo.class, foos);
		final FixjureFactory fact = FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).enableOption(Fixjure.Option.SKIP_UNMAPPABLE);
		final Foo foo = fact.createFixture(Foo.class, "only");
		assertNotNull(foo);
		assertEquals("Foo Bar", foo.getName());
	}

	@Test(expected = FixtureException.class)
	public void disableOption() {
		final Map<String, byte[]> foos = ImmutableMap.of("only", "{name:'Foo Bar', value:'Does Not Map'}".getBytes());
		final Map<Class<?>, Map<String, byte[]>> mem = ImmutableMap.<Class<?>, Map<String,byte[]>>of(Foo.class, foos);
		final FixjureFactory fact = FixjureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem)).enableOption(Fixjure.Option.SKIP_UNMAPPABLE).disableOption(Fixjure.Option.SKIP_UNMAPPABLE);
		fact.createFixture(Foo.class, "only");
	}
}
