package com.bigfatgun.fixjures.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class StringBuilderFixtureHandlerTest {

	private StringBuilderFixtureHandler handler = new StringBuilderFixtureHandler();

	@Test
	public void theTypeIsRight() {
		assertSame(String.class, handler.getSourceType());
		assertSame(StringBuilder.class, handler.getReturnType());
	}

	@Test
	public void stringWorks() {
		assertEquals(toString(), handler.apply(toString()).toString());
	}
}
