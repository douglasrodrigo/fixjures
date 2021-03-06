package com.bigfatgun.fixjures.annotations;

import com.bigfatgun.fixjures.SourceFactory;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.json.JsonSourceFactory;
import com.bigfatgun.fixjures.serializable.ObjectInputStreamSourceFactory;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * An enumeration of source formats supported by the core library. Every source format is smart enough to know how to
 * create a {@link com.bigfatgun.fixjures.SourceFactory} from a {@link com.bigfatgun.fixjures.annotations.Fixture}
 * annotation.
 *
 * @author Steve Reed
 */
public enum NativeSourceFormat implements SourceFormat {

	/** {@code java.io.Serializable}. */
	IoSerializable {
		/**
		 * Creates a new serializable source factory.
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public SourceFactory createSourceFactory(final ClassLoader fixtureClassLoader, final SourceType sourceType) {
			return ObjectInputStreamSourceFactory.newFactoryFromSourceStrategy(new Strategies.SourceStrategy() {
                @Override
                public ReadableByteChannel findStream(Class<?> type, String name) throws IOException {
                    return sourceType.openStream(fixtureClassLoader, name);
                }
            });
		}},

	/** JSON. */
	Json {
		/**
		 * Creates a new json source factory.
		 * <p>
		 * {@inheritDoc}
		 */
		@Override
		public SourceFactory createSourceFactory(final ClassLoader fixtureClassLoader, final SourceType sourceType) {
			return JsonSourceFactory.newFactoryFromSourceStrategy(new Strategies.SourceStrategy() {
                @Override
                public ReadableByteChannel findStream(Class<?> type, String name) throws IOException {
                    return sourceType.openStream(fixtureClassLoader, name);
                }
            });
		}
	};

	/**
	 * Creates a format-specific source factory with a classloader and source type.
	 *
	 * @param fixtureClassLoader class loader to use
	 * @param sourceType fixture source type
	 * @return new source factory
	 */
	public abstract SourceFactory createSourceFactory(final ClassLoader fixtureClassLoader, final SourceType sourceType);
}
