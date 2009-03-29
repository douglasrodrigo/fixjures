package com.bigfatgun.fixjures;

import org.junit.Test;
import static org.junit.Assert.assertNull;
import com.google.common.collect.ImmutableMap;

public class SourceFixtureBuilderTest {

	@Test
	public void whenTheObjectIsTheWrongType() {
		assertNull(Fixjure.of(String.class).from(new FixtureSource() {
			@Override
			public <T> SourcedFixtureBuilder<T> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T>(builder) {
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
			public <T> SourcedFixtureBuilder<T> build(final FixtureBuilder<T> builder) {
				return new SourcedFixtureBuilder<T>(builder) {
					@Override
					protected Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> classFixtureHandlerImmutableMap) throws Exception {
						throw new Exception();
					}
				};
			}
		}).create());
	}
}
