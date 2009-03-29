package com.bigfatgun.fixjures.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureHandler;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

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
	public void simpleStringLiteralToInterface() {
		final Foo foo = Fixjure.of(Foo.class).from(new JSONSource("{ \"bar\" : \"" + toString() + "\" }")).create();
		assertNotNull(foo);
		assertEquals(toString(), foo.getBar());
	}

	@Test
	public void simpleStringLiteralToConcrete() {
		final FooTwo foo = Fixjure.of(FooTwo.class).from(new JSONSource("{ \"electric\" : \"boogaloo\" }")).create();
		assertNotNull(foo);
		assertEquals("boogaloo", foo.getElectric());
	}

	@Test
	public void simpleStringLiteralToConcreteWithPrivateConstructor() {
		final FooThree foo = Fixjure.of(FooThree.class).from(new JSONSource("{ \"electric\" : \"boogaloo\" }")).create();
		assertNotNull(foo);
		assertEquals("boogaloo", foo.getElectric());
	}

	@Test
	public void complexFromFile() throws FileNotFoundException {
		final Complex complex = Fixjure.of(Complex.class).from(new JSONSource(new File("src/test/resources/json1.txt"))).create();
		assertEquals("Some String!", complex.getStr());
		assertEquals((byte) 4, complex.getByte());
		assertEquals((Double) 1e14, complex.getDouble());
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
	public void complexWithFixtureHandler() {
		final Complex complex = Fixjure.of(Complex.class)
				  .from(new JSONSource(" { \"str\" : \"val\" } "))
				  .with(new FixtureHandler<String, String>() {
					  public Class<String> getType() {
						  return String.class;
					  }

					  public String deserialize(final Class desiredType, final String rawValue, final String name) {
						  return "overridden!";
					  }
				  })
				  .create();
		assertEquals("overridden!", complex.getStr());
	}

	@Test
	public void unsupportedJSONValue() {
		assertNull(Fixjure.of(Boolean.class).from(new JSONSource(" true ")).create());
	}

	public static interface WithNonGenericList {
		List getFoo();
	}

	@Test
	public void nonGenericList() {
		final WithNonGenericList foo = Fixjure.of(WithNonGenericList.class).from(new JSONSource("{ \"foo\" : [ 1, 2 ] }")).create();
		assertNull(foo.getFoo());
	}

	@Test(expected = FileNotFoundException.class)
	public void fileNotFoundOnBadFile() throws FileNotFoundException {
		new JSONSource(new File("foo"));
	}

	@Test(expected = ClassCastException.class)
	public void youGetErrorsWhenYouUseAFixtureHandlerToHijackYourStuff() {
		Fixjure.of(Complex.class).from(new JSONSource("{ str : \"str\"}")).with(new FixtureHandler() {
			public Class getType() {
				return String.class;
			}

			public Object deserialize(final Class desiredType, final Object rawValue, final String name) {
				return 1;
			}
		}).create().getStr();
	}

	@Test
	public void attemptToConvertListToUnknownCollectionType() {
		assertNull(Fixjure.of(Complex.class).from(new JSONSource("{ str : [1, 2] }")).create().getStr());
	}

	private static class BadCtor {
		public BadCtor() {
			throw new RuntimeException("worst constructor ever.");
		}
	}

	@Test
	public void exceptionOnConcreteCtor() {
		assertNull(Fixjure.of(BadCtor.class).from(new JSONSource("{ 1: 2 }")).create());
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

	@Test
	public void badGettersAndSetters() {
		BadGettersAndSetters bgas = Fixjure.of(BadGettersAndSetters.class).from(new JSONSource("{ \"unknown\" : 1, \"bar\" : 2, \"foo\" : 3 }")).create();
		assertNotNull(bgas);
		assertNull(bgas.getFoo());
		assertNull(bgas.getBar());
	}
}
