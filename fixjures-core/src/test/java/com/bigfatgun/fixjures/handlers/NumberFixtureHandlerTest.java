package com.bigfatgun.fixjures.handlers;

import static org.junit.Assert.assertSame;
import org.junit.Test;

public class NumberFixtureHandlerTest {

	private NumberUnmarshaller handler;

	@Test
	public void theTypeIsRight() {
		handler = new NumberUnmarshaller<Number>(Number.class, Number.class) {
			@Override
			protected Number narrowNumericValue(final Number number) {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}
		};
		assertSame(Number.class, handler.getSourceType());
	}
}
