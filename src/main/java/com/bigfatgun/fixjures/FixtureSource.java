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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.List;

import com.bigfatgun.fixjures.handlers.ByteFixtureHandler;
import com.bigfatgun.fixjures.handlers.DoubleFixtureHandler;
import com.bigfatgun.fixjures.handlers.FloatFixtureHandler;
import com.bigfatgun.fixjures.handlers.IntegerFixtureHandler;
import com.bigfatgun.fixjures.handlers.LongFixtureHandler;
import com.bigfatgun.fixjures.handlers.NoConversionFixtureHandler;
import com.bigfatgun.fixjures.handlers.ShortFixtureHandler;
import com.bigfatgun.fixjures.handlers.StringBuilderFixtureHandler;
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
	private final Multimap<Class, FixtureHandler> requiredTypeHandlers;

	/**
	 * Raw data.
	 */
	private final ReadableByteChannel sourceChannel;

	/**
	 * Map of source type to fixture handlers.
	 */
	private final Multimap<Class, FixtureHandler> sourceTypeHandlers;

	/**
	 * Initializes the source.
	 *
	 * @param source source data
	 */
	protected FixtureSource(final ReadableByteChannel source) {
		sourceChannel = source;
		requiredTypeHandlers = Multimaps.newLinkedHashMultimap();
		sourceTypeHandlers = Multimaps.newLinkedHashMultimap();
		installDefaultHandlers();
	}

	/**
	 * Install default fixture handlers.
	 */
	private void installDefaultHandlers() {
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(String.class));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.class));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Byte.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Short.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Integer.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Long.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Float.TYPE));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Double.TYPE));
		installSourceTypeHandler(new ByteFixtureHandler());
		installSourceTypeHandler(new ShortFixtureHandler());
		installSourceTypeHandler(new IntegerFixtureHandler());
		installSourceTypeHandler(new LongFixtureHandler());
		installSourceTypeHandler(new FloatFixtureHandler());
		installSourceTypeHandler(new DoubleFixtureHandler());
		installSourceTypeHandler(new StringBuilderFixtureHandler());
		installDesiredTypeHandler(new ByteFixtureHandler());
		installDesiredTypeHandler(new ShortFixtureHandler());
		installDesiredTypeHandler(new IntegerFixtureHandler());
		installDesiredTypeHandler(new LongFixtureHandler());
		installDesiredTypeHandler(new FloatFixtureHandler());
		installDesiredTypeHandler(new DoubleFixtureHandler());
		installDesiredTypeHandler(new StringBuilderFixtureHandler());
	}

	/**
	 * Exposes the source data to sub-classes.
	 *
	 * @return source data
	 */
	protected final ReadableByteChannel getSource() {
		return sourceChannel;
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
	 * Closes the source channel.
	 * <p>
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		sourceChannel.close();
	}

	/**
	 * Exposes map of source type handlers to subclasses.
	 *
	 * @return immutable multimap of source type to fixture handler
	 */
	protected final ImmutableMultimap<Class, FixtureHandler> getSourceTypeHandlers() {
		return ImmutableMultimap.copyOf(sourceTypeHandlers);
	}

	/**
	 * Exposes map of desired type handlers to subclasses.
	 *
	 * @return immutable multimap of desired type to fixture handler
	 */
	protected final ImmutableMultimap<Class, FixtureHandler> getRequiredTypeHandlers() {
		return ImmutableMultimap.copyOf(requiredTypeHandlers);
	}

	/**
	 * Installs a desired type handler by mapping its return type to itself.
	 *
	 * @param handler handler to install
	 */
	protected final void installDesiredTypeHandler(final FixtureHandler handler) {
		requiredTypeHandlers.put(handler.getReturnType(), handler);
	}

	/**
	 * Installs a source type fixture handler by mapping its source type to itself.
	 *
	 * @param handler handler to install
	 */
	protected final void installSourceTypeHandler(final FixtureHandler handler) {
		sourceTypeHandlers.put(handler.getSourceType(), handler);
	}

	/**
	 * @param type object type
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 */
	protected final Object findValue(final Type type, final Object value, final String name) throws Exception {
		final Class getterClass;
		final Type[] typeParams;
		if (type instanceof ParameterizedType) {
			//noinspection unchecked
			getterClass = (Class) ((ParameterizedType) type).getRawType();
			//noinspection unchecked
			typeParams = ((ParameterizedType) type).getActualTypeArguments();
		} else {
			//noinspection unchecked
			getterClass = (Class) type;
			typeParams = new Type[0];
		}
		return findValue(getterClass, Arrays.asList(typeParams), value, name);
	}

	/**
	 * @param type object type
	 * @param typeParams object type's type params
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 */
	protected final Object findValue(final Class type, final List<? extends Type> typeParams, final Object value, final String name) throws Exception {
		for (Class cls = type; cls != null; cls = cls.getSuperclass()) {
			for (final FixtureHandler handler : getRequiredTypeHandlers().get(cls)) {
				if (handler.canDeserialize(value, type)) {
					//noinspection unchecked
					return handler.apply(value);
				}
			}
		}

		for (Class cls = value.getClass(); cls != null; cls = cls.getSuperclass()) {
			for (final FixtureHandler handler : getSourceTypeHandlers().get(cls)) {
				if (handler.canDeserialize(value, type)) {
					//noinspection unchecked
					return handler.apply(value);
				}
			}
		}

		return handle(type, typeParams, value, name);
	}

	protected Object handle(final Class type, final List<? extends Type> typeParams, final Object value, final String name) throws Exception {
		return null;
	}
}
