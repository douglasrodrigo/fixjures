package com.bigfatgun.fixjures;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
		final String string = Fixjure.of(String.class).from(new JSONSource("foo")).create();
		assertEquals("foo", string);
	}

	@Test
	public void listFixture() {
		List<Integer> expected = Lists.newArrayList(1, 2, 3, 4, 5);
		List<Integer> actual1 = Fixjure.of(List.class).of(Integer.class).from(new JSONSource("[ 1, 2, 3, 4, 5 ]")).create();
		List<Integer> actual2 = Fixjure.listOf(Integer.class).from(new JSONSource("[ 1, 2, 3, 4, 5 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	@Test
	public void setFixture() {
		Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
		Set<Integer> actual1 = Fixjure.of(Set.class).of(Integer.class).from(new JSONSource("[ 1, 1, 2, 3, 4, 3, 5 ]")).create();
		Set<Integer> actual2 = Fixjure.setOf(Integer.class).from(new JSONSource("[ 5, 4, 3, 2, 1 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}

	@Test
	public void multisetFixture() {
		Multiset<Integer> expected = ImmutableMultiset.of(1, 1, 2, 3, 5, 8, 13);
		Multiset<Integer> actual1 = Fixjure.of(Multiset.class).of(Integer.class).from(new JSONSource("[ 1, 1, 2, 3, 5, 8, 13 ]")).create();
		Multiset<Integer> actual2 = Fixjure.multisetOf(Integer.class).from(new JSONSource("[ 13, 8, 5, 3, 2, 1, 1 ]")).create();
		assertEquals(expected, actual1);
		assertEquals(expected, actual2);
	}
}
