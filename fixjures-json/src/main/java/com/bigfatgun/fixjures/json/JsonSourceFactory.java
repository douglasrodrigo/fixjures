package com.bigfatgun.fixjures.json;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;

public final class JsonSourceFactory extends AbstractSourceFactory {

	public static JsonSourceFactory newFactoryFromSourceStrategy(final Strategies.SourceStrategy strategy) {
		return new JsonSourceFactory(strategy);
	}

	private JsonSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return JSONSource.newJsonStream(this.loadFixtureDataSource(fixtureType, fixtureId));
	}
}
