package com.bigfatgun.fixjures.annotations;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.bigfatgun.fixjures.ByteUtil;

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
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String resourceName) throws FileNotFoundException {
			final InputStream stream = clsLoader.getResourceAsStream(resourceName);
			if (stream == null) {
				throw new FileNotFoundException(resourceName);
			} else {
				return Channels.newChannel(stream);
			}
		}
	},

	/**
	 * File.
	 */
	File {
		/**
		 * Opens a stream of a file with the given name.
		 *
		 * @param clsLoader ignored
		 * @param filename file name
		 * @return file bytes
		 * @throws FileNotFoundException if the file is not found
		 */
		@Override
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String filename) throws FileNotFoundException {
			return new RandomAccessFile(filename, "r").getChannel();
		}
	},

	/**
	 * String literal.
	 */
	Literal {
		/**
		 * Returns the string literal as a byte stream.
		 *
		 * @param literal string
		 * @return string bytes
		 */
		@Override
		public ReadableByteChannel openStream(final ClassLoader clsLoader, final String literal) {
			return Channels.newChannel(new ByteArrayInputStream(ByteUtil.getBytes(literal)));
		}
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract ReadableByteChannel openStream(final ClassLoader clsLoader, final String value) throws IOException;
}
