package com.bigfatgun.fixjures.serializable;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.annotations.SourceType;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class ObjectInputStreamSourceFactory extends AbstractSourceFactory {

	public static ObjectInputStreamSourceFactory newFactoryFromSourceType(final ClassLoader fixtureClassLoader, final SourceType dataSourceType) {
		return new ObjectInputStreamSourceFactory(new Strategies.SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				return dataSourceType.openStream(fixtureClassLoader, name);
			}
		});
	}

	private ObjectInputStreamSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return ObjectInputStreamSource.newObjectInputStream(loadFixtureDataSource(fixtureType, fixtureId));
	}
}
