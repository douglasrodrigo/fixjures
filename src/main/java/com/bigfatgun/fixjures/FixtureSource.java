/*
 * Copyright (C) 2009 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bigfatgun.fixjures;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * Abstract fixture source which provides a no-op implementation of
 * {@code java.io.Closeable.close()}.
 *
 * @author Steve Reed
 */
public abstract class FixtureSource implements Closeable {

	/** Charset to use when reading byte streams and channels. */
	private static final String CHARSET = "UTF-8";

	/**
	 * Reads the entire contents of the given byte channel into a string builder. The channel is
	 * still open after this method returns.
	 *
	 * @param channel channel to read, will NOT be closed before the method returns
	 * @return string contents of channel
	 * @throws IOException if there are any IO errors while reading or closing the given channel
	 */
	public static String loadTextFromChannel(final ReadableByteChannel channel) throws IOException {
		try {
			final ByteBuffer buf = ByteBuffer.allocate(Short.MAX_VALUE);
			final CharsetDecoder decoder = Charset.forName(CHARSET).newDecoder();
			final StringBuilder string = new StringBuilder();

			while (channel.read(buf) != -1) {
				buf.flip();
				string.append(decoder.decode(buf));
				buf.clear();
			}

			return string.toString();
		} finally {
			channel.close();
		}
	}

	/**
	 * Converts the given string into a UTF-8 encoded byte array.
	 *
	 * @param str string to convert
	 * @return byte array
	 */
	public static byte[] getBytes(final String str) {
		try {
			return str.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JSONSource requires UTF-8.");
		}
	}

	/**
	 * Map of desired type to fixture handlers.
	 */
	private final Multimap<Class, FixtureHandler> desiredTypeHandlers;

	/**
	 * Initializes the source.
	 */
	public FixtureSource() {
		desiredTypeHandlers = Multimaps.newLinkedHashMultimap();
	}

	/**
	 * Converts the given builder into a "sourced" fixture builder.
	 *
	 * @param <T> fixture object type
	 * @param builder the builder to convert
	 * @return sourced fixture builder
	 */
	public abstract <T> SourcedFixtureBuilder<T, ? extends FixtureSource> build(FixtureBuilder<T> builder);

	/**
	 * No-op.
	 * <p>
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		// nothing to do, override this
	}

	/**
	 * Exposes map of desired type handlers to subclasses.
	 *
	 * @return immutable multimap of desired type to fixture handler
	 */
	protected ImmutableMultimap<Class, FixtureHandler> getDesiredTypeHandlers() {
		return ImmutableMultimap.copyOf(desiredTypeHandlers);
	}

	/**
	 * Installs a desired type handler by mapping its return type to itself.
	 *
	 * @param handler handler to install
	 */
	protected void installDesiredTypeHandler(final FixtureHandler handler) {
		desiredTypeHandlers.put(handler.getReturnType(), handler);
	}
}
