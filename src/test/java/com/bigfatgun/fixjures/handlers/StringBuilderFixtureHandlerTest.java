package com.bigfatgun.fixjures.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StringBuilderFixtureHandlerTest {

	private StringBuilderFixtureHandler handler = new StringBuilderFixtureHandler();

	@Test
	public void theTypeIsRight() {
		assertSame(CharSequence.class, handler.getSourceType());
		assertSame(StringBuilder.class, handler.getReturnType());
	}

	@Test
	public void stringWorks() {
		assertEquals(toString(), handler.apply(null, toString()).toString());
	}

	@Test
	public void canDeserialize() {
		assertTrue(handler.canDeserialize("foo", StringBuilder.class));
		assertTrue(handler.canDeserialize(new StringBuilder("foo"), StringBuilder.class));
		assertTrue(handler.canDeserialize(null, StringBuilder.class));
		assertFalse(handler.canDeserialize("foo", String.class));
		assertFalse(handler.canDeserialize(null, String.class));
		assertFalse(handler.canDeserialize("foo", CharSequence.class));
		assertFalse(handler.canDeserialize(null, CharSequence.class));
		assertFalse(handler.canDeserialize(new StringBuilder("foo"), String.class));
		assertFalse(handler.canDeserialize(new StringBuilder("foo"), CharSequence.class));
		assertFalse(handler.canDeserialize("foo", Byte.class));
	}
}
