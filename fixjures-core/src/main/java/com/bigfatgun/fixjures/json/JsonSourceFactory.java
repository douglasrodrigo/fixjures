package com.bigfatgun.fixjures.json;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.annotations.SourceType;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public final class JsonSourceFactory extends AbstractSourceFactory {

	public static JsonSourceFactory newFactoryFromSourceType(final ClassLoader fixtureClassLoader, final SourceType dataSourceType) {
		return new JsonSourceFactory(new Strategies.SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				return dataSourceType.openStream(fixtureClassLoader, name);
			}
		});
	}

	private JsonSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return JSONSource.newJsonStream(this.loadFixtureDataSource(fixtureType, fixtureId));
	}
}
