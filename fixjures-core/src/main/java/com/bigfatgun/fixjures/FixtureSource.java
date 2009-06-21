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
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Set;
import javax.annotation.Nullable;

import static com.bigfatgun.fixjures.FixtureTypeDefinition.wrap;
import com.bigfatgun.fixjures.handlers.AbstractFixtureHandler;
import com.bigfatgun.fixjures.handlers.ChainedFixtureHandler;
import com.bigfatgun.fixjures.handlers.FixtureHandler;
import com.bigfatgun.fixjures.handlers.HandlerHelper;
import com.bigfatgun.fixjures.handlers.Handlers;
import com.bigfatgun.fixjures.handlers.NoConversionFixtureHandler;
import com.bigfatgun.fixjures.handlers.PrimitiveHandler;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
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
public abstract class FixtureSource implements Closeable, HandlerHelper {

	private static final String CHARSET = "UTF-8";

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

	private static final ImmutableSet<Fixjure.Option> DEFAULT_OPTIONS = ImmutableSet.of();

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
			throw FixtureException.convert("JSONSource requires UTF-8.", e);
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

	private final Multimap<Class<?>, FixtureHandler<?>> typeHandlers;
	private final ReadableByteChannel sourceChannel;
	private final Set<Fixjure.Option> options;
	@Nullable private IdentityResolver identityResolver;

	protected FixtureSource(final ReadableByteChannel source) {
		sourceChannel = Preconditions.checkNotNull(source);
		typeHandlers = LinkedListMultimap.create();
		installDefaultHandlers();
		options = Sets.newEnumSet(DEFAULT_OPTIONS, Fixjure.Option.class);
	}

	/**
	 * Adds an option.
	 * @param opt option
	 */
	public final void addOption(final Fixjure.Option opt) {
		checkNotNull(opt);
		options.add(opt);
	}

	public void close() throws IOException {
		sourceChannel.close();
	}

	public final ImmutableSet<Fixjure.Option> getOptions() {
		return ImmutableSet.copyOf(options);
	}

	protected abstract Object createFixture(final FixtureTypeDefinition type);

	void setIdentityResolver(@Nullable final IdentityResolver resolver) {
		this.identityResolver = resolver;
	}

	private boolean canHandleIdentity(final Class<?> type, @Nullable final Object rawIdentityValue) {
		return identityResolver != null
				  && rawIdentityValue != null
				  && identityResolver.canHandleIdentity(type, rawIdentityValue);
	}

	private <T> ValueProvider<T> resolveIdentity(final Class<T> type, final Object rawIdentityValue) {
		if (canHandleIdentity(type, rawIdentityValue)) {
			assert identityResolver != null : "Don't attempt to resolve an id if the resolver is null!";
			return ValueProviders.ofIdentity(identityResolver, type, rawIdentityValue);
		} else {
			return null;
		}
	}

	protected final ImmutableMultimap<Class<?>, FixtureHandler<?>> getTypeHandlers() {
		return ImmutableMultimap.copyOf(typeHandlers);
	}

	protected final ReadableByteChannel getSource() {
		return sourceChannel;
	}

	protected final void installTypeHandler(final FixtureHandler<?> handler) {
		typeHandlers.put(handler.getReturnType(), handler);
		if (handler instanceof PrimitiveHandler) {
			final PrimitiveHandler primitiveHandler = (PrimitiveHandler) handler;
			typeHandlers.put(primitiveHandler.getPrimitiveType(), handler);
		}
	}

	protected final boolean isOptionEnabled(final Fixjure.Option option) {
		return options.contains(option);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final FixtureHandler<?> findHandler(final Object src, final FixtureTypeDefinition type) {
		final Class<?> cls = type.getType();

		if (cls.isInstance(src)) {
			return NoConversionFixtureHandler.newInstance(cls);
		}

		for (Class<?> keyClass = cls; keyClass != null; keyClass = keyClass.getSuperclass()) {
			for (final FixtureHandler<?> handler : getTypeHandlers().get(keyClass)) {
				if (handler.canDeserialize(src, cls)) {
					return handler;
				}
			}
		}

		for (final FixtureHandler<?> handler : getTypeHandlers().get(Object.class)) {
			if (handler.canDeserialize(src, cls)) {
				return handler;
			}
		}

		if (cls.isArray()) {
			for (final FixtureHandler<?> handler : getTypeHandlers().get(Object[].class)) {
				if (handler.canDeserialize(src, cls)) {
					return handler;
				}
			}
		}

		//noinspection unchecked
		return new AbstractFixtureHandler(Object.class, cls) {
			@Override
			public ValueProvider<?> apply(final HandlerHelper helper, final FixtureTypeDefinition typeDef, final Object source) {
				return resolveIdentity(cls, src);
			}
		};
	}

	/**
	 * @param type object type
	 * @param value object value
	 * @return proxied object
	 */
	protected final ValueProvider<?> findValue(final FixtureTypeDefinition type, final Object value) {
		final FixtureHandler<?> handler = findHandler(value, type);
		return handler.apply(this, type, value);
	}

	/**
	 * Install default fixture handlers.
	 */
	private void installDefaultHandlers() {
		installTypeHandler(NoConversionFixtureHandler.newInstance(String.class));
		installTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.class));
		installTypeHandler(NoConversionFixtureHandler.newInstance(Boolean.TYPE));

		for (final Class<?> t : NUMERIC_TYPES) {
			installTypeHandler(NoConversionFixtureHandler.newInstance(t));
		}

		final FixtureHandler<Byte> byteHandler = Handlers.byteHandler();
		final FixtureHandler<Short> shortHandler = Handlers.shortHandler();
		final FixtureHandler<Integer> intHandler = Handlers.integerHandler();
		final FixtureHandler<Long> longHandler = Handlers.longHandler();
		final FixtureHandler<Float> floatHandler = Handlers.floatHandler();
		final FixtureHandler<Double> doubleHandler = Handlers.doubleHandler();
		final FixtureHandler<BigInteger> bigintHandler = Handlers.bigIntegerHandler();
		final FixtureHandler<BigDecimal> bigdecHandler = Handlers.bigDecimalHandler();

		installTypeHandler(byteHandler);
		installTypeHandler(shortHandler);
		installTypeHandler(intHandler);
		installTypeHandler(longHandler);
		installTypeHandler(floatHandler);
		installTypeHandler(doubleHandler);
		installTypeHandler(bigintHandler);
		installTypeHandler(bigdecHandler);
		final ChainedFixtureHandler<Number> chainedHandler = new ChainedFixtureHandler<Number>(CharSequence.class, Number.class) {
			@Override
			public ValueProvider<? extends Number> apply(final HandlerHelper helper, final FixtureTypeDefinition typeDef, final Object source) {
				return ValueProviders.of(Double.parseDouble(castSourceValue(CharSequence.class, source).toString()));
			}
		};
		installTypeHandler(chainedHandler.link(byteHandler));
		installTypeHandler(chainedHandler.link(shortHandler));
		installTypeHandler(chainedHandler.link(intHandler));
		installTypeHandler(chainedHandler.link(longHandler));
		installTypeHandler(chainedHandler.link(floatHandler));
		installTypeHandler(chainedHandler.link(doubleHandler));
		installTypeHandler(chainedHandler.link(bigintHandler));
		installTypeHandler(chainedHandler.link(bigdecHandler));

		installTypeHandler(Handlers.stringBuilderHandler());
		installTypeHandler(Handlers.javaDateHandler());
	}
}
