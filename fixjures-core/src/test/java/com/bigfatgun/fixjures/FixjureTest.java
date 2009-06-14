package com.bigfatgun.fixjures;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static com.bigfatgun.fixjures.Fixjure.Option.SKIP_UNMAPPABLE;
import com.bigfatgun.fixjures.json.JSONSource;
import com.bigfatgun.fixjures.serializable.ObjectInputStreamSource;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class FixjureTest {

	@Test
	public void constructorIsPrivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Constructor<Fixjure> ctor = Fixjure.class.getDeclaredConstructor();
		assertFalse(ctor.isAccessible());
		ctor.setAccessible(true);
		assertNotNull(ctor.newInstance());
	}

	@Test
	public void plainFixture() {
		final String string = Fixjure.of(String.class).from(JSONSource.newJsonString("foo")).create();
		assertEquals("foo", string);
	}

	@Test
	public void listFixture() {
		List<Integer> expected = Lists.newArrayList(1, 2, 3, 4, 5);
		List<Integer> actual1 = Fixjure.of(List.class).of(Integer.class).from(JSONSource.newJsonString("[ 1, 2, 3, 4, 5 ]")).create();
		List<Integer> actual2 = Fixjure.listOf(Integer.class).from(JSONSource.newJsonString("[ 1, 2, 3, 4, 5 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	@Test
	public void setFixture() {
		Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
		Set<Integer> actual1 = Fixjure.of(Set.class).of(Integer.class).from(JSONSource.newJsonString("[ 1, 1, 2, 3, 4, 3, 5 ]")).create();
		Set<Integer> actual2 = Fixjure.setOf(Integer.class).from(JSONSource.newJsonString("[ 5, 4, 3, 2, 1 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	@Test
	public void multisetFixture() {
		Multiset<Integer> expected = ImmutableMultiset.of(1, 1, 2, 3, 5, 8, 13);
		Multiset<Integer> actual1 = Fixjure.of(Multiset.class).of(Integer.class).from(JSONSource.newJsonString("[ 1, 1, 2, 3, 5, 8, 13 ]")).create();
		Multiset<Integer> actual2 = Fixjure.multisetOf(Integer.class).from(JSONSource.newJsonString("[ 13, 8, 5, 3, 2, 1, 1 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	@Test
	public void mapFixture() {
		Map<String, Integer> expected = ImmutableMap.of("one", 1, "two", 2, "three", 3);
		Map<String, Integer> actual1 = Fixjure.of(Map.class).of(String.class, Integer.class).from(JSONSource.newJsonString("{ one : 1, two : 2, three : 3 }")).create();
		Map<String, Integer> actual2 = Fixjure.mapOf(String.class, Integer.class).from(JSONSource.newJsonString("{ one : 1, two : 2, three : 3 }")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	private static interface NyTimesChannel {
		String getTitle();
	}

	private static interface NyTimes {
		NyTimesChannel getChannel();
		String getVersion();
	}

	@Test
	public void urlFixture() {
		try {
			final String nytimesJsonUrl = "http://prototype.nytimes.com/svc/widgets/dataservice.html?uri=http://www.nytimes.com/services/xml/rss/nyt/World.xml";
			final FixtureSource src = JSONSource.newRemoteUrl(new URL(nytimesJsonUrl));
			NyTimes nytimes = Fixjure.of(NyTimes.class).from(src).withOptions(SKIP_UNMAPPABLE).create();
			System.out.println("Successfully connected to nytimes, got version: " + nytimes.getVersion());
			assertEquals("NYT > World", nytimes.getChannel().getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("You need access to nytimes.com for this test to work.");
		}
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

	@Test
	public void factory() {
		FixtureFactory fact = FixtureFactory.newFactory(new SourceFactory() {
			public FixtureSource newInstance(final Class<?> type, final String name) {
				try {
					return JSONSource.newJsonResource(FixjureTest.class.getClassLoader(), String.format("fixjures/%s/%s.json", type.getName(), name));
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		});
		fact.enableOption(SKIP_UNMAPPABLE);
		final NyTimes n1 = fact.createFixture(NyTimes.class, "one");
		assertNotNull(n1);
		assertEquals("1.0", n1.getVersion());
		final NyTimes n2 = fact.createFixture(NyTimes.class, "two");
		assertNotNull(n2);
		assertEquals("2.0", n2.getVersion());
		// test cache
		assertSame(n2, fact.createFixture(NyTimes.class, "two"));
		// clear cache
		fact.expireCache();
		assertNotSame(n2, fact.createFixture(NyTimes.class, "two"));
	}

	@Test
	public void factoryWithStrategy() {
		FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newClasspathStrategy(Strategies.DEFAULT_CLASSPATH_NAME_STRATEGY));
		fact.enableOption(SKIP_UNMAPPABLE);
		final NyTimes n1 = fact.createFixture(NyTimes.class, "one");
		assertNotNull(n1);
		assertEquals("1.0", n1.getVersion());
		final NyTimes n2 = fact.createFixture(NyTimes.class, "two");
		assertNotNull(n2);
		assertEquals("2.0", n2.getVersion());
		// test cache
		assertSame(n2, fact.createFixture(NyTimes.class, "two"));
		// clear cache
		fact.expireCache();
		assertNotSame(n2, fact.createFixture(NyTimes.class, "two"));
	}

	@Test
	public void inMemoryStrategy() {
		Map<Class<?>, Map<String, String>> mem = Maps.newHashMap();
		mem.put(String.class, ImmutableMap.of("foo", "foo", "bar", "bar"));
		mem.put(Integer.class, ImmutableMap.of("one", "1", "two", "2"));
		mem.put(Map.class, ImmutableMap.of("map", "{ one : 1, two : 2 }"));
		mem.put(NyTimes.class, ImmutableMap.of("nyt", "{ version: '2.1' }"));
		FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(mem));
		assertEquals("foo", fact.createFixture(String.class, "foo"));
		assertEquals("bar", fact.createFixture(String.class, "bar"));
		assertEquals(1, fact.createFixture(Integer.class, "one").intValue());
		assertEquals(2, fact.createFixture(Integer.class, "two").intValue());
		assertEquals(ImmutableMap.of("one", 1, "two", 2), fact.createFixture(Map.class, "map"));
		assertEquals("2.1", fact.createFixture(NyTimes.class, "nyt").getVersion());
	}

	private <T> double doPerf(final FixtureFactory fact, final Class<T> type, final String name, final int num, final boolean clearCache, final String desc) {
		System.out.format("Creating %d of %s named %s [%s]...\n", num, type.getName(), name, desc);
		final long start = System.nanoTime();
		T obj;
		for (int i = 0; i < num; i++) {
			//noinspection UnusedAssignment
			obj = fact.createFixture(type, name);
			if (i != 0 && (i % 50000) == 0) {
				System.out.println(obj);
				final long dur = System.nanoTime() - start;
				System.out.format("At %d, average of %gms/fixture.\n", i, ((dur / 1e6) / i));
			}
			if (clearCache) {
				fact.expireCache();
			}
		}
		final long dur = System.nanoTime() - start;
		System.out.format("At %d, average of %gms/fixture.\n", num, ((dur / 1e6) / num));
		return ((dur / 1e6) / num);
	}

	@Test
	public void perf() {
		FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newClasspathStrategy());
		double cpwc = doPerf(fact, NyTimes.class, "one", 1000000, false, "classpath w/ cache");
		double cpwoc = doPerf(fact, NyTimes.class, "one", 1000, true, "classpath w/o cache");
		fact = FixtureFactory.newFactory(new SourceFactory() {
			public FixtureSource newInstance(final Class<?> type, final String name) {
				try {
					return JSONSource.newJsonResource(FixjureTest.class.getClassLoader(), String.format("fixjures/%s/%s.json", type.getName(), name));
				} catch (FileNotFoundException e) {
					throw convert(e);
				}
			}
		});
		double fwc = doPerf(fact, NyTimes.class, "one", 1000000, false, "file w/ cache");
		double fwoc = doPerf(fact, NyTimes.class, "one", 1000, true, "file w/o cache");
		// asserting that file-based without cache is faster than classpath-based without cache
		assertTrue(Double.compare(cpwoc, fwoc) > 0);
	}

	@Test
	public void datetime() {
		System.out.println(DateFormat.getDateTimeInstance().format(new Date()));
		Date d = Fixjure.of(Date.class).from(JSONSource.newJsonString("Apr 11, 2009 9:29:20 PM")).create();
		assertNotNull(d);
		final Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		assertEquals(3, cal.get(Calendar.MONTH));
		assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(2009, cal.get(Calendar.YEAR));
	}

	private static interface Thing {
		Thing getNext();
	}

	@Test
	public void anonymousIdentityResolver() {
		final Thing expected = new Thing() {
			@Override
			public Thing getNext() {
				return null;
			}
		};
		Thing t = Fixjure.of(Thing.class).from(JSONSource.newJsonString("{next:'_'}")).resolveIdsWith(new IdentityResolver() {
			@Override
			public boolean canHandleIdentity(final Class<?> requiredType, @Nullable final Object rawIdentityValue) {
				return "_".equals(rawIdentityValue);
			}

			@Override
			public String coerceIdentity(@Nullable final Object rawIdentityValue) {
				return (String) rawIdentityValue;
			}

			@Override
			public <T> T resolve(final Class<T> requiredType, final String id) {
				return requiredType.cast(("_".equals(id)) ? expected : null);
			}
		}).create();
		assertNotNull(t);
		assertSame(expected, t.getNext());
		assertNull(t.getNext().getNext());
	}
}
