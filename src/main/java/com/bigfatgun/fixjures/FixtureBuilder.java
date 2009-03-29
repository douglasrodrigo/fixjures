package com.bigfatgun.fixjures;

/**
 * Your basic fixture builder that provides the simplest implementation of
 * the {@code from} method, which passes the type info into a fixture source
 * in order to create a {@link SourcedFixtureBuilder}.
 *
 * @param <T> fixture object type
 */
public class FixtureBuilder<T> {

	/** Fixture object type. */
	private final Class<T> clazz;

	/**
	 * Instantiates a new fixture builder.
	 *
	 * @param cls fixture object type
	 */
	/* package */ FixtureBuilder(final Class<T> cls) {
		clazz = cls;
	}

	/**
	 * Uses the given {@code FixtureSource} to convert this builder into
	 * a {@code SourcedFixtureBuilder}.
	 *
	 * @param source fixture source, could be JSON or otherwise
	 * @return sourced fixture builder
	 */
	public SourcedFixtureBuilder<T> from(final FixtureSource source) {
		return source.build(this);
	}

	/**
	 * @return fixture object type
	 */
	public final Class<T> getType() {
		return clazz;
	}
}
