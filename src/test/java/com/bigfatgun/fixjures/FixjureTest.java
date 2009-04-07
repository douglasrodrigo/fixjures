package com.bigfatgun.fixjures;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bigfatgun.fixjures.Fixjure.Option.SKIP_UNMAPPABLE;
import com.bigfatgun.fixjures.json.JSONSource;
import com.bigfatgun.fixjures.serializable.ObjectInputStreamSource;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
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
		String s = Fixjure.of(String.class).from(ObjectInputStreamSource.newFile(new File("src/test/resources/string1.ser"))).create();
		assertEquals("abcdefghijklm\nopqrs\tuvwxyz", s);
	}

	@Test
	public void serializableTest2() throws IOException {
		final List<String> list = new LinkedList<String>();
		Iterables.addAll(list, Fixjure.of(String.class).fromStream(ObjectInputStreamSource.newFile(new File("src/test/resources/string2.ser"))).createAll());
		assertEquals(2, list.size());
		assertEquals("first", list.get(0));
		assertEquals("second", list.get(1));
	}
}
