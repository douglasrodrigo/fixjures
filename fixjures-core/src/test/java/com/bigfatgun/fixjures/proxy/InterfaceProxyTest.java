package com.bigfatgun.fixjures.proxy;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableSet;
import static org.junit.Assert.*;
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
		new InterfaceProxy<Foo>(Foo.class, ImmutableSet.<Fixjure.Option>of());
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeMethodWithArgs() {
		new InterfaceProxy<Foo2>(Foo2.class, ImmutableSet.<Fixjure.Option>of()).create().convertThing(this);
	}

	@Test(expected = RuntimeException.class)
	public void doNotInvokeUnstubbedMethod() {
		new InterfaceProxy<Foo2>(Foo2.class, ImmutableSet.<Fixjure.Option>of()).create().getThing();
	}

	private static interface Foo3 {
		String getStr();
	}

	@Test
	public void proxiesUseIdentityForEquals() {
		final Foo3 foo1 = new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).create();
		final Foo3 foo2 = new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).create();
		assertEquals(foo1, foo1);
		assertFalse(foo1.equals(foo2));
	}

	@Test
	public void proxiesHaveAHashCode() throws Exception {
		Fixjure.of(Foo3.class).from(JSONSource.newJsonString("{ str : \"09182340\" }")).create().hashCode();
	}

	@Test
	public void proxiesHaveToStr() {
		assertTrue(new InterfaceProxy<Foo3>(Foo3.class, ImmutableSet.<Fixjure.Option>of()).create().toString().startsWith("Proxy of " + Foo3.class));
	}
}
