package com.bigfatgun.fixjures;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.bigfatgun.fixjures.annotations.NativeSourceType;
import static org.junit.Assert.*;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class FixtureInjectorTest {

	@Test
	public void testMyBean() throws Exception {
		MyBean bean = new MyBean();
		assertNull(bean.getFoo());
		assertNull(bean.getBar());
		assertNull(bean.getParent());
		FixtureInjector.scan(bean);
		assertNotNull(bean.getParent());
		assertEquals("value of foo parent", bean.getParent().getFoo());
		assertEquals(4321, bean.getParent().getBar().intValue());
		assertEquals(1234, bean.getBar().intValue());
		assertEquals("value of foo", bean.getFoo());
		assertEquals("firstsecond", bean.getFirstAndSecond());
		assertNotNull(bean.getParent().getParent());
		assertNull(bean.getParent().getParent().getBar());
		assertNull(bean.getParent().getParent().getParent());
		assertEquals("value of foo grandparent", bean.getParent().getParent().getFoo());
	}

	@Test
	public void constructorIsPrivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Constructor<FixtureInjector> ctor = FixtureInjector.class.getDeclaredConstructor();
		assertFalse(ctor.isAccessible());
		ctor.setAccessible(true);
		assertNotNull(ctor.newInstance());
	}

	@Test(expected = FixtureException.class)
	public void badMarkup1() throws Exception {
		FixtureInjector.scan(new BadBean1());
	}

	@Test(expected = FixtureException.class)
	public void badMarkup2() throws Exception {
		FixtureInjector.scan(new BadBean2());
	}

	@Test(expected = FixtureException.class)
	public void badMarkup3() throws Exception {
		FixtureInjector.scan(new BadBean3());
	}

	public static final class MyBean {

		private String foo;

		private Integer bar;

		private MyBean parent;

		private String first, second;

		public String getFirstAndSecond() {
			return first + second;
		}

		public void setFirstAndSecond(@Fixture(value = "\"first\"") final String s1,
				@Fixture(value = "\"second\"") final String s2) {
			first = s1;
			second = s2;
		}

		public String getFoo() {
			return foo;
		}

		@Fixture(value = "\"value of foo\"")
		public void setFoo(final String foo) {
			this.foo = foo;
		}

		public Integer getBar() {
			return bar;
		}

		@Fixture(value = "1234")
		public void setBar(final Integer bar) {
			this.bar = bar;
		}

		public MyBean getParent() {
			return parent;
		}

		@Fixture(type = NativeSourceType.Resource, value = "JSONFixtureHelper.MyBean.json")
		public void setParent(final MyBean parent) {
			this.parent = parent;
		}
	}

	private static class BadBean1 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@Fixture(value = "foo")
		public void setFoo(final String value, final boolean oopsie) {
			foo = value;
		}
	}

	private static class BadBean2 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@Fixture(value = "1234")
		public void setFoo(final String value) {
			foo = value;
		}
	}

	private static class BadBean3 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@Fixture(value = "1234")
		public BadBean3 setFoo(final String value) {
			foo = value;
			return this;
		}
	}

}
