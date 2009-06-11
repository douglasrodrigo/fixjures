package com.bigfatgun.fixjures;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Abstract base class for other source factories that can be easily implemented on top of
 * a {@link java.nio.channels.ReadableByteChannel}.
 */
public abstract class AbstractSourceFactory implements SourceFactory {

	/** Strategy for finding source data based on object type and id. */
	private final Strategies.SourceStrategy dataSourceStrategy;

	/**
	 * Saves the data source strategy.
	 *
	 * @param dataSourceStrategy non-null data source strategy
	 */
	protected AbstractSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		if (dataSourceStrategy == null) {
			throw new NullPointerException("dataSourceStrategy");
		}

		this.dataSourceStrategy = dataSourceStrategy;
	}

	/**
	 * Uses the data source strategy to find fixture source data, and wraps any {@code IOException}s with
	 * a {@link com.bigfatgun.fixjures.FixtureException}.
	 *
	 * @param fixtureObjectType fixture object type
	 * @param fixtureId object id
	 * @return source data channel
	 */
	protected ReadableByteChannel loadFixtureDataSource(final Class<?> fixtureObjectType, final String fixtureId) {
		assert fixtureObjectType != null : "Fixture object type must not be null.";
		assert fixtureId != null : "Fixture id must not be null.";

		try {
			return dataSourceStrategy.findStream(fixtureObjectType, fixtureId);
		} catch (IOException e) {
			throw new FixtureException(e);
		}
	}
}
