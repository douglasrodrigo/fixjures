package com.bigfatgun.fixjures.proxy;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		new InterfaceProxy<Foo>(Foo.class);
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeMethodWithArgs() {
		new InterfaceProxy<Foo2>(Foo2.class).create().convertThing(this);
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeUnstubbedMethod() {
		new InterfaceProxy<Foo2>(Foo2.class).create().getThing();
	}

	private static interface Foo3 {
		String getStr();
	}

	@Test
	public void proxiesUseIdentityForEquals() {
		final Foo3 foo1 = new InterfaceProxy<Foo3>(Foo3.class).create();
		final Foo3 foo2 = new InterfaceProxy<Foo3>(Foo3.class).create();
		assertEquals(foo1, foo1);
		assertFalse(foo1.equals(foo2));
	}

	@Test
	public void proxiesHaveAHashCodeBasedOnValue() throws Exception {
		final Foo3 foo = Fixjure.of(Foo3.class).from(new JSONSource("{ str : \"09182340\" }")).create();
		assertEquals(ImmutableSet.of("09182340").hashCode(), foo.hashCode());
	}

	@Test
	public void proxiesHaveToStr() {
		assertEquals("Proxy of " + Foo3.class, new InterfaceProxy<Foo3>(Foo3.class).create().toString());
	}
}
