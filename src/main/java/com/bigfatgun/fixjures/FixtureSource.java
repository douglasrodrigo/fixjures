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
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Set;
import javax.annotation.Nullable;

import com.bigfatgun.fixjures.handlers.ChainedFixtureHandler;
import com.bigfatgun.fixjures.handlers.FixtureHandler;
import com.bigfatgun.fixjures.handlers.Handlers;
import com.bigfatgun.fixjures.handlers.NoConversionFixtureHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Abstract fixture source.
 *
 * @author Steve Reed
 */
public abstract class FixtureSource implements Closeable {

	/** Charset to use when reading byte streams and channels. */
	private static final String CHARSET = "UTF-8";

	/** Array of numeric types. */
	private static final ImmutableSet<Class<?>> NUMERIC_TYPES = ImmutableSet.<Class<?>>of(
			  Byte.class,
			  Byte.TYPE,
			  Short.class,
			  Short.TYPE,
			  Integer.class,
			  Integer.TYPE,
			  Long.class,
			  Long.TYPE,
			  Float.class,
			  Float.TYPE,
			  Double.class,
			  Double.TYPE
	);

	/** Default options, currently none. */
	private static final ImmutableSet<Fixjure.Option> DEFAULT_OPTIONS = ImmutableSet.of();

	/**
	 * Converts the given string into a UTF-8 encoded byte array.
	 *
	 * @param str string to convert
	 * @return byte array
	 */
	protected static byte[] getBytes(final String str) {
		try {
			return str.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new FixtureException("JSONSource requires UTF-8.");
		}
	}

	/**
	 * Reads the entire contents of the given byte channel into a string builder. The channel is
	 * still open after this method returns.
	 *
	 * @param channel channel to read, will NOT be closed before the method returns
	 * @return string contents of channel
	 * @throws IOException if there are any IO errors while reading or closing the given channel
	 */
	protected static String loadTextFromChannel(final ReadableByteChannel channel) throws IOException {
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
	 * Map of desired type to fixture handlers.
	 */
	private final Multimap<Class<?>, FixtureHandler<?,?>> requiredTypeHandlers;

	/**
	 * Raw data.
	 */
	private final ReadableByteChannel sourceChannel;

	/**
	 * Map of source type to fixture handlers.
	 */
	private final Multimap<Class<?>, FixtureHandler<?,?>> sourceTypeHandlers;

	/**
	 * Set of options.
	 */
	private final Set<? super Fixjure.Option> options;

	/**
	 * Initializes the source.
	 *
	 * @param source source data
	 */
	protected FixtureSource(final ReadableByteChannel source) {
		if (source == null) {
			throw new NullPointerException("source");
		}

		sourceChannel = source;
		requiredTypeHandlers = LinkedListMultimap.create();
		sourceTypeHandlers = LinkedListMultimap.create();
		installDefaultHandlers();
		options = Sets.newEnumSet(DEFAULT_OPTIONS, Fixjure.Option.class);
	}

	/**
	 * Adds an option.
	 * @param opt option
	 */
	public void addOption(final Fixjure.Option opt) {
		options.add(opt);
	}

	/**
	 * Closes the source channel.
	 * <p>
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		sourceChannel.close();
	}

	/**
	 * Creates a fixture object from source.
	 *
	 * @param type fixture object type
	 * @param typeParams type params
	 * @param <T> fixture object type
	 * @return new fixture object
	 */
	public abstract <T> T createFixture(final Class<T> type, final ImmutableList<Class<?>> typeParams);

	/**
	 * Exposes map of desired type handlers to subclasses.
	 *
	 * @return immutable multimap of desired type to fixture handler
	 */
	protected final ImmutableMultimap<Class<?>, FixtureHandler<?,?>> getRequiredTypeHandlers() {
		return ImmutableMultimap.copyOf(requiredTypeHandlers);
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
	 * Exposes map of source type handlers to subclasses.
	 *
	 * @return immutable multimap of source type to fixture handler
	 */
	protected final ImmutableMultimap<Class<?>, FixtureHandler<?,?>> getSourceTypeHandlers() {
		return ImmutableMultimap.copyOf(sourceTypeHandlers);
	}

	/**
	 * Installs a desired type handler by mapping its return type to itself.
	 *
	 * @param handler handler to install
	 */
	protected final void installRequiredTypeHandler(final FixtureHandler<?,?> handler) {
		requiredTypeHandlers.put(handler.getReturnType(), handler);
	}

	/**
	 * @param option option to test
	 * @return true if option is enabled
	 */
	protected final boolean isOptionEnabled(final Fixjure.Option option) {
		return options.contains(option);
	}

	/**
	 * @param type object type
	 * @param typeVariable object type's type param if applicable
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 */
	protected final Object findValue(final Type type, final Type typeVariable, final Object value, final String name) {
		final Class<?> getterClass;
		final Type[] typeParams;
		if (type instanceof ParameterizedType) {
			getterClass = (Class<?>) ((ParameterizedType) type).getRawType();
			typeParams = ((ParameterizedType) type).getActualTypeArguments();
		} else if (type instanceof TypeVariable && typeVariable != null) {
			getterClass = (Class) typeVariable;
			typeParams = new Type[0];
		} else {
			getterClass = (Class) type;
			typeParams = new Type[0];
		}
		return findValue(getterClass, ImmutableList.of(typeParams), value, name);
	}

	/**
	 * @param type object type
	 * @param typeParams object type's type params
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 */
	protected final Object findValue(final Class type, final ImmutableList<? extends Type> typeParams, final Object value, final String name) {
		for (Class<?> cls = type; cls != null; cls = cls.getSuperclass()) {
			for (final FixtureHandler handler : getRequiredTypeHandlers().get(cls)) {
				if (handler.canDeserialize(value, type)) {
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

	/**
	 * Unimplements source value conversion handler method. Subclasses should override this to catch
	 * any source-specific types and perform their own conversion there.
	 *
	 * @param requiredType required type
	 * @param typeParams required type params, never null
	 * @param sourceValue source value
	 * @param name value name, for logging
	 * @return value
	 */
	protected Object handle(final Class<?> requiredType, final ImmutableList<? extends Type> typeParams, final Object sourceValue, final String name) {
		return null;
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
	 * Installs a fixture handler for both source and required type.
	 *
	 * @param handler handler
	 */
	protected final void installUniversalHandler(final FixtureHandler<?,?> handler) {
		installRequiredTypeHandler(handler);
		installSourceTypeHandler(handler);
	}

	/**
	 * Install default fixture handlers.
	 */
	private void installDefaultHandlers() {
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(String.class));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.class));
		installSourceTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.TYPE));

		for (final Class<?> t : NUMERIC_TYPES) {
			installSourceTypeHandler(NoConversionFixtureHandler.newInstance(t));
		}

		final FixtureHandler<Number,Byte> byteHandler = Handlers.byteHandler();
		final FixtureHandler<Number,Short> shortHandler = Handlers.shortHandler();
		final FixtureHandler<Number,Integer> intHandler = Handlers.integerHandler();
		final FixtureHandler<Number,Long> longHandler = Handlers.longHandler();
		final FixtureHandler<Number,Float> floatHandler = Handlers.floatHandler();
		final FixtureHandler<Number,Double> doubleHandler = Handlers.doubleHandler();
		final FixtureHandler<Number, BigInteger> bigintHandler = Handlers.bigIntegerHandler();
		final FixtureHandler<Number, BigDecimal> bigdecHandler = Handlers.bigDecimalHandler();

		installUniversalHandler(byteHandler);
		installUniversalHandler(shortHandler);
		installUniversalHandler(intHandler);
		installUniversalHandler(longHandler);
		installUniversalHandler(floatHandler);
		installUniversalHandler(doubleHandler);
		installUniversalHandler(bigintHandler);
		installUniversalHandler(bigdecHandler);
		final ChainedFixtureHandler<CharSequence,Number> chainedHandler = new ChainedFixtureHandler<CharSequence, Number>() {
			public Class<Number> getReturnType() {
				return Number.class;
			}

			public Class<CharSequence> getSourceType() {
				return CharSequence.class;
			}

			public Number apply(@Nullable final CharSequence charSequence) {
				return Double.parseDouble(charSequence.toString());
			}
		};
		installUniversalHandler(chainedHandler.link(byteHandler));
		installUniversalHandler(chainedHandler.link(shortHandler));
		installUniversalHandler(chainedHandler.link(intHandler));
		installUniversalHandler(chainedHandler.link(longHandler));
		installUniversalHandler(chainedHandler.link(floatHandler));
		installUniversalHandler(chainedHandler.link(doubleHandler));
		installUniversalHandler(chainedHandler.link(bigintHandler));
		installUniversalHandler(chainedHandler.link(bigdecHandler));

		installRequiredTypeHandler(Handlers.stringBuilderHandler());
		installRequiredTypeHandler(Handlers.javaDateHandler());
	}
}
