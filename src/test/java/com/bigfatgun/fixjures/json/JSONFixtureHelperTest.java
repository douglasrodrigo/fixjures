package com.bigfatgun.fixjures.json;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class JSONFixtureHelperTest {

	@Test
	public void testMyBean() throws InvocationTargetException, FileNotFoundException, IllegalAccessException {
		MyBean bean = new MyBean();
		assertNull(bean.getFoo());
		assertNull(bean.getBar());
		assertNull(bean.getParent());
		JSONFixtureHelper.scan(bean);
		assertNotNull(bean.getParent());
		assertEquals("value of foo parent", bean.getParent().getFoo());
		assertEquals(4321, bean.getParent().getBar().intValue());
		assertEquals(1234, bean.getBar().intValue());
		assertEquals("value of foo", bean.getFoo());
		assertNotNull(bean.getParent().getParent());
		assertNull(bean.getParent().getParent().getBar());
		assertNull(bean.getParent().getParent().getParent());
		assertEquals("value of foo grandparent", bean.getParent().getParent().getFoo());
	}

	@Test
	public void constructorIsPrivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Constructor<JSONFixtureHelper> ctor = JSONFixtureHelper.class.getDeclaredConstructor();
		assertFalse(ctor.isAccessible());
		ctor.setAccessible(true);
		assertNotNull(ctor.newInstance());
	}

	@Test
	public void badMarkup1() throws InvocationTargetException, FileNotFoundException, IllegalAccessException {
		JSONFixtureHelper.scan(new BadBean1());
	}

	@Test(expected = IllegalArgumentException.class)
	public void badMarkup2() throws InvocationTargetException, FileNotFoundException, IllegalAccessException {
		JSONFixtureHelper.scan(new BadBean2());
	}

	@Test
	public void badMarkup3() throws InvocationTargetException, FileNotFoundException, IllegalAccessException {
		JSONFixtureHelper.scan(new BadBean3());
	}

	public static final class MyBean {

		private String foo;

		private Integer bar;

		private MyBean parent;

		public String getFoo() {
			return foo;
		}

		@JSONFixture(type = JSONSource.SourceType.LITERAL, value = "value of foo")
		public void setFoo(final String foo) {
			this.foo = foo;
		}

		public Integer getBar() {
			return bar;
		}

		@JSONFixture(value = "1234")
		public void setBar(final Integer bar) {
			this.bar = bar;
		}

		public MyBean getParent() {
			return parent;
		}

		@JSONFixture(type = JSONSource.SourceType.FILE, value = "src/test/resources/JSONFixtureHelper.MyBean.json")
		public void setParent(final MyBean parent) {
			this.parent = parent;
		}
	}

	private static class BadBean1 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@JSONFixture(value = "foo")
		public void setFoo(final String value, final boolean oopsie) {
			foo = value;
		}
	}

	private static class BadBean2 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@JSONFixture(value = "1234")
		public void setFoo(final String value) {
			foo = value;
		}
	}

	private static class BadBean3 {

		private String foo;

		public String getFoo() {
			return foo;
		}

		@JSONFixture(value = "1234")
		public BadBean3 setFoo(final String value) {
			foo = value;
			return this;
		}
	}

}
