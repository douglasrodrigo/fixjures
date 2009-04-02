package com.bigfatgun.fixjures.handlers;

import com.google.common.base.Nullable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class NumberFixtureHandlerTest {

	private NumberFixtureHandler handler;

	@Test
	public void theTypeIsRight() {
		handler = new NumberFixtureHandler<Number>() {
			@Override
			protected Class getPrimitiveType() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			@Override
			public Class getReturnType() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public Number apply(@Nullable final Number o) {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}
		};
		assertSame(Number.class, handler.getSourceType());
	}

	@Test
	public void handleByte() {
		handler = new ByteFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals((byte) i, handler.apply(i));
			assertEquals((byte) i, handler.apply(i));
		}
	}

	@Test
	public void handleShort() {
		handler = new ShortFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals((short) i, handler.apply(i));
			assertEquals((short) i, handler.apply(i));
		}
	}

	@Test
	public void handleInt() {
		handler = new IntegerFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals((int) i, handler.apply(i));
			assertEquals((int) i, handler.apply(i));
		}
	}

	@Test
	public void handleLong() {
		handler = new LongFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals(i, handler.apply(i));
			assertEquals(i, handler.apply(i));
		}
	}

	@Test
	public void handleFloat() {
		handler = new FloatFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals((float) i, handler.apply(i));
			assertEquals((float) i, handler.apply(i));
		}
	}

	@Test
	public void handleDouble() {
		handler = new DoubleFixtureHandler();
		for (long i = -100; i < 100; i++) {
			assertEquals((double) i, handler.apply(i));
		}
	}
}
