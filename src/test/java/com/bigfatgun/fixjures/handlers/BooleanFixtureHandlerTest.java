package com.bigfatgun.fixjures.handlers;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BooleanFixtureHandlerTest {

	private BooleanFixtureHandler handler = new BooleanFixtureHandler();

	@Test
	public void theTypeIsRight() {
		assertSame(Boolean.class, handler.getType());
	}

	@Test
	public void trueWorks() {
		assertTrue(handler.deserialize(Boolean.class, true, "true"));
	}

	@Test
	public void falseWorks() {
		assertFalse(handler.deserialize(Boolean.class, false, "false"));
	}

	@Test
	public void nullOnWrongObject() {
		assertNull(handler.deserialize(Boolean.class, "true", "true string"));
	}
}
