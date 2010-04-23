package com.bigfatgun.fixjures.serializable;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;

public class ObjectInputStreamSourceFactory extends AbstractSourceFactory {

	public static ObjectInputStreamSourceFactory newFactoryFromSourceStrategy(final Strategies.SourceStrategy strategy) {
		return new ObjectInputStreamSourceFactory(strategy);
	}

	private ObjectInputStreamSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return ObjectInputStreamSource.newObjectInputStream(loadFixtureDataSource(fixtureType, fixtureId));
	}
}
