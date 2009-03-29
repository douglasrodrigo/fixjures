package com.bigfatgun.fixjures.proxy;

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
}
