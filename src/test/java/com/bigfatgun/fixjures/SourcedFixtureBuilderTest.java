package com.bigfatgun.fixjures;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class SourcedFixtureBuilderTest {

	@Test(expected = RuntimeException.class)
	public void whenAnExceptionIsThrown() throws Exception {
		assertNull(Fixjure.of(Integer.class).from(new FixtureSource() {
			@Override
			public <T> SourcedFixtureBuilder<T, FixtureSource> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T, FixtureSource>(builder, this) {
					@Override
					protected T createFixtureObject() {
						throw new RuntimeException();
					}
				};
			}
		}).create());
	}

	@Test
	public void whenSourceThrowsExceptionOnClose() throws Exception {
		assertEquals((Integer) 1234, Fixjure.of(Integer.class).from(new FixtureSource() {
			@Override
			public void close() throws IOException {
				throw new IOException("told you");
			}

			@Override
			public <T> SourcedFixtureBuilder<T, FixtureSource> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T, FixtureSource>(builder, this) {
					@Override
					protected T createFixtureObject() {
						return builder.getType().cast(1234);
					}
				};
			}
		}).create());
	}
}
