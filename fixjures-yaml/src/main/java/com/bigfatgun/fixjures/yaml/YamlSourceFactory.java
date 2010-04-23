package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.AbstractSourceFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;

public final class YamlSourceFactory extends AbstractSourceFactory {

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
