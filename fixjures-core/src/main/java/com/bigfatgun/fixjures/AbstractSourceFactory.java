package com.bigfatgun.fixjures;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Abstract base class for other source factories that can be easily implemented on top of a {@link
 * java.nio.channels.ReadableByteChannel}.
 */
public abstract class AbstractSourceFactory implements SourceFactory {

	private final Strategies.SourceStrategy dataSourceStrategy;

	protected AbstractSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		checkNotNull(dataSourceStrategy);
		this.dataSourceStrategy = dataSourceStrategy;
	}

	/**
	 * Uses the data source strategy to find fixture source data, and wraps any {@code IOException}s with a {@link
	 * com.bigfatgun.fixjures.FixtureException}.
	 *
	 * @param fixtureObjectType fixture object type
	 * @param fixtureId object id
	 * @return source data channel
	 */
	protected ReadableByteChannel loadFixtureDataSource(final Class<?> fixtureObjectType, final String fixtureId) {
		checkNotNull(fixtureObjectType);
		checkNotNull(fixtureId);

		try {
			return dataSourceStrategy.findStream(fixtureObjectType, fixtureId);
		} catch (IOException e) {
			throw FixtureException.convert(e);
		}
	}
}
