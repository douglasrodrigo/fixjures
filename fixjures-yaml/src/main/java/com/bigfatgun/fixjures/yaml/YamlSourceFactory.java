package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.annotations.SourceType;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public final class YamlSourceFactory extends AbstractSourceFactory {

	public static YamlSourceFactory newFactoryFromSourceType(final ClassLoader fixtureClassLoader, final SourceType dataSourceType) {
		return newFactory(new Strategies.SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				return dataSourceType.openStream(fixtureClassLoader, name);
			}
		});
	}

	public static YamlSourceFactory newFactory(final Strategies.SourceStrategy sourceStrategy) {
		return new YamlSourceFactory(sourceStrategy);
	}

	private YamlSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		super(dataSourceStrategy);
	}

	public FixtureSource newInstance(final Class<?> fixtureType, final String fixtureId) {
		return YamlSource.newYamlStream(this.loadFixtureDataSource(fixtureType, fixtureId));
	}
}
