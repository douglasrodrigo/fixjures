package com.bigfatgun.fixjures.annotations;

import java.io.IOException;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.SourceFactory;
import com.bigfatgun.fixjures.json.JSONSource;

/**
 * An enumeration of source formats.
 *
 * @author Steve Reed
 */
public enum NativeSourceFormat implements SourceFormat {

	/**
	 * JSON.
	 */
	Json {
		/**
		 * Creates a new json source factory.
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public SourceFactory createSourceFactory(final ClassLoader clsLoader, final Fixture fixtureAnnotation) {
			return new SourceFactory() {
				public FixtureSource newInstance(final Class<?> type, final String name) {
					try {
						return JSONSource.newJsonStream(fixtureAnnotation.type().openStream(clsLoader, fixtureAnnotation.value()));
					} catch (IOException e) {
						throw new FixtureException(e);
					}
				}
			};
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public abstract SourceFactory createSourceFactory(ClassLoader clsLoader, Fixture fixtureAnnotation);
}
