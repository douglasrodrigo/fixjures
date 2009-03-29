package com.bigfatgun.fixjures;

import java.io.Closeable;
import java.io.IOException;

/**
 * Abstract fixture source which provides a no-op implementation of
 * {@code java.io.Closeable.close()}.
 */
public abstract class FixtureSource implements Closeable {

	/**
	 * Converts the given builder into a "sourced" fixture builder.
	 *
	 * @param <T> fixture object type
	 * @param builder the builder to convert
	 * @return sourced fixture builder
	 */
	public abstract <T> SourcedFixtureBuilder<T> build(FixtureBuilder<T> builder);

	/**
	 * No-op.
	 * <p>
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		// nothing to do, override this
	}
}
