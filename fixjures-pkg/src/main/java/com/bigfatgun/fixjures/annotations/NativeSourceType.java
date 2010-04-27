package com.bigfatgun.fixjures.annotations;

import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * An enumeration of source types.
 *
 * @author Steve Reed
 */
public enum NativeSourceType implements SourceType {

	Resource {
		/**
		 * Opens a resource using this classes classloader.
		 * <p>
		 * {@inheritDoc}
		 */
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String resourceName) {
			final InputStream stream = clsLoader.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new FixtureException("Unable to locate resource named " + resourceName);
			} else {
				return Channels.newChannel(stream);
			}
		}
	},

	/** File. */
	File {
		/**
		 * Opens a stream of a file with the given name.
		 *
		 * @param clsLoader ignored
		 * @param filename file name
		 * @return file bytes
		 */
		@Override
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String filename) {
			try {
				return new RandomAccessFile(filename, "r").getChannel();
			} catch (FileNotFoundException e) {
				throw FixtureException.convert(e);
			}
		}
	},

	/** String literal. */
	Literal {
		/**
		 * Returns the string literal as a byte stream.
		 *
		 * @param literal string
		 * @return string bytes
		 */
		@Override
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String literal) {
            return Channels.newChannel(new ByteArrayInputStream(literal.getBytes(Charsets.UTF_8)));
        }
	};

	/** {@inheritDoc} */
	public abstract ReadableByteChannel openStream(final ClassLoader clsLoader, final String value);
}
