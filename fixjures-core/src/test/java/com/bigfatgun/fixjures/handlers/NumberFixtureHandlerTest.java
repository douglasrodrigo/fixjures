package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;
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

			public ValueProvider<Number> apply(final HandlerHelper helper, final Number o) {
				return ValueProviders.of(null);
			}
		};
		assertSame(Number.class, handler.getSourceType());
	}

	@Test
	public void handleByte() {
		handler = new ByteFixtureHandler();
		assertSame(Byte.TYPE, handler.getPrimitiveType());
		assertSame(Byte.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals((byte) i, handler.apply(null, i).get());
			assertEquals((byte) i, handler.apply(null, i).get());
		}
	}

	@Test
	public void handleShort() {
		handler = new ShortFixtureHandler();
		assertSame(Short.TYPE, handler.getPrimitiveType());
		assertSame(Short.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals((short) i, handler.apply(null, i).get());
			assertEquals((short) i, handler.apply(null, i).get());
		}
	}

	@Test
	public void handleInt() {
		handler = new IntegerFixtureHandler();
		assertSame(Integer.TYPE, handler.getPrimitiveType());
		assertSame(Integer.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals((int) i, handler.apply(null, i).get());
			assertEquals((int) i, handler.apply(null, i).get());
		}
	}

	@Test
	public void handleLong() {
		handler = new LongFixtureHandler();
		assertSame(Long.TYPE, handler.getPrimitiveType());
		assertSame(Long.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals(i, handler.apply(null, i).get());
			assertEquals(i, handler.apply(null, i).get());
		}
	}

	@Test
	public void handleFloat() {
		handler = new FloatFixtureHandler();
		assertSame(Float.TYPE, handler.getPrimitiveType());
		assertSame(Float.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals((float) i, handler.apply(null, i).get());
			assertEquals((float) i, handler.apply(null, i).get());
		}
	}

	@Test
	public void handleDouble() {
		handler = new DoubleFixtureHandler();
		assertSame(Double.TYPE, handler.getPrimitiveType());
		assertSame(Double.class, handler.getReturnType());
		assertSame(Number.class, handler.getSourceType());
		for (long i = -100; i < 100; i++) {
			assertEquals((double) i, handler.apply(null, i).get());
		}
	}
}
