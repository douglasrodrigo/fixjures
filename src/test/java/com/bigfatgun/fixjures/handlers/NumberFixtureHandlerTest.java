package com.bigfatgun.fixjures.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class NumberFixtureHandlerTest {

	private NumberFixtureHandler handler = new NumberFixtureHandler();

	@Test
	public void theTypeIsRight() {
		assertSame(Number.class, handler.getType());
	}

	@Test
	public void handleByte() {
		for (long i = -100; i < 100; i++) {
			assertEquals((byte) i, handler.deserialize(Byte.class, i, "i"));
			assertEquals((byte) i, handler.deserialize(Byte.TYPE, i, "i"));
		}

		assertFalse(handler.deserialize(Short.class, 100, "one hundred").equals((byte) 100));
	}

	@Test
	public void handleShort() {
		for (long i = -100; i < 100; i++) {
			assertEquals((short) i, handler.deserialize(Short.class, i, "i"));
			assertEquals((short) i, handler.deserialize(Short.TYPE, i, "i"));
		}

		assertFalse(handler.deserialize(Integer.class, 100000, "one hundred k").equals((short) 100000));
	}

	@Test
	public void handleInt() {
		for (long i = -100; i < 100; i++) {
			assertEquals((int) i, handler.deserialize(Integer.class, i, "i"));
			assertEquals((int) i, handler.deserialize(Integer.TYPE, i, "i"));
		}

		assertFalse(handler.deserialize(Long.class, 4000000000L, "one hundred k").equals((int) 4000000000L));
	}

	@Test
	public void handleLong() {
		for (long i = -100; i < 100; i++) {
			assertEquals(i, handler.deserialize(Long.class, i, "i"));
			assertEquals(i, handler.deserialize(Long.TYPE, i, "i"));
		}
	}

	@Test
	public void handleFloat() {
		for (long i = -100; i < 100; i++) {
			assertEquals((float) i, handler.deserialize(Float.class, i, "i"));
			assertEquals((float) i, handler.deserialize(Float.TYPE, i, "i"));
		}

		assertFalse(handler.deserialize(Double.class, 100000.0d, ".").equals((float) 100000.0d));
	}

	@Test
	public void handleDouble() {
		for (long i = -100; i < 100; i++) {
			assertEquals((double) i, handler.deserialize(Double.class, i, "one"));
		}
	}
}
