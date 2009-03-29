package com.bigfatgun.fixjures;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class FixtureSourceTest {

	@Test
	public void closeMethodSeemsToDoNothing() throws IOException {
		new FixtureSource() {

			@Override
			public void close() throws IOException {
				super.close();
				assertTrue(true);
			}

			@Override
			public <T> SourcedFixtureBuilder<T> build(final FixtureBuilder<T> builder) {
				throw new RuntimeException("Not meant to be invoked.");
			}
		}.close();
	}
}
