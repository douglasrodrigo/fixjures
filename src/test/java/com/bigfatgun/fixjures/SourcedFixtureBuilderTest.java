package com.bigfatgun.fixjures;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import com.google.common.collect.ImmutableMap;

public class SourcedFixtureBuilderTest {

	@Test
	public void whenTheObjectIsTheWrongType() {
		assertNull(Fixjure.of(String.class).from(new FixtureSource() {
			@Override
			public <T> SourcedFixtureBuilder<T, FixtureSource> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T, FixtureSource>(builder, this) {
					@Override
					protected Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> classFixtureHandlerImmutableMap) throws Exception {
						return 1;
					}
				};
			}
		}).create());
	}

	@Test
	public void whenAnExceptionIsThrown() {
		assertNull(Fixjure.of(Integer.class).from(new FixtureSource() {
			@Override
			public <T> SourcedFixtureBuilder<T, FixtureSource> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T, FixtureSource>(builder, this) {
					@Override
					protected Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> classFixtureHandlerImmutableMap) throws Exception {
						throw new Exception();
					}
				};
			}
		}).create());
	}

	@Test
	public void whenSourceThrowsExceptionOnClose() {
		assertEquals((Integer) 1234, Fixjure.of(Integer.class).from(new FixtureSource() {
			@Override
			public void close() throws IOException {
				throw new IOException("told you");
			}

			@Override
			public <T> SourcedFixtureBuilder<T, FixtureSource> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T, FixtureSource>(builder, this) {
					@Override
					protected Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> classFixtureHandlerImmutableMap) throws Exception {
						return 1234;
					}
				};
			}
		}).create());
	}
}
