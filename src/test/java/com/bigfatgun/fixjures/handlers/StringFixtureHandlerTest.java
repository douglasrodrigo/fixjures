package com.bigfatgun.fixjures.handlers;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class StringFixtureHandlerTest {

	private StringFixtureHandler handler = new StringFixtureHandler();

	@Test
	public void theTypeIsRight() {
		assertSame(String.class, handler.getType());
	}

	@Test
	public void stringWorks() {
		assertEquals(toString(), handler.deserialize(String.class, toString(), "toString()"));
		assertEquals("1234", handler.deserialize(Integer.class, 1234, "1234 Integer"));
	}

	@Test
	public void nullOnTypeMismatch() {
		assertNull(handler.deserialize(String.class, 1234, "1234 Integer"));
	}
}
