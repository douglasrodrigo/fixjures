package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.TypeWrapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class StringBuilderFixtureHandlerTest {

	private StringBuilderUnmarshaller handler = new StringBuilderUnmarshaller();

	@Test
	public void theTypeIsRight() {
		assertSame(CharSequence.class, handler.getSourceType());
		assertSame(StringBuilder.class, handler.getReturnType());
	}

	@Test
	public void stringWorks() {
		assertEquals(toString(), handler.unmarshall(null, toString(), TypeWrapper.wrap(StringBuilder.class)).get().toString());
	}
}
