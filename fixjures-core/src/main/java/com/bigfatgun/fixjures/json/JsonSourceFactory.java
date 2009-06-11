package com.bigfatgun.fixjures.json;

import java.nio.channels.ReadableByteChannel;
import java.io.IOException;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.annotations.SourceType;

public final class JsonSourceFactory extends AbstractSourceFactory {
	
	public static JsonSourceFactory newFactoryFromSourceType(final ClassLoader fixtureClassLoader, final SourceType dataSourceType) {
		return new JsonSourceFactory(new Strategies.SourceStrategy() {
			@Override
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				return dataSourceType.openStream(fixtureClassLoader, name);
			}
		});
	}

	private JsonSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	@Override
	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return JSONSource.newJsonStream(this.loadFixtureDataSource(fixtureType, fixtureId));
	}
}
