package com.bigfatgun.fixjures;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class FixtureExceptionTest {

	@Test
	public void testMessageCtor() {
		assertEquals("the message", new FixtureException("the message").getMessage());
		assertNull(new FixtureException("the message").getCause());
	}

	@Test
	public void testCauseCtor() {
		assertEquals("java.lang.RuntimeException: the message", FixtureException.convert(new RuntimeException("the message")).getMessage());
		assertNotNull(FixtureException.convert(new RuntimeException()).getCause());
	}

	@Test
	public void convertFixtureException() {
		final FixtureException expected = new FixtureException("msg");
		assertSame(expected, FixtureException.convert(expected));
	}

	@Test
	public void convertNull() {
		assertNotNull(FixtureException.convert(null));
	}

	@Test
	public void convertRte() {
		final RuntimeException expected = new RuntimeException();
		assertSame(expected, FixtureException.convert(expected).getCause());
	}
}
