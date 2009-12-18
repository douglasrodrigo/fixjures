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

import com.bigfatgun.fixjures.handlers.*;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Supplier;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Abstract fixture source.
 *
 * @author Steve Reed
 */
public abstract class FixtureSource implements Closeable, UnmarshallingContext {

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

	private final Multimap<Class<?>, Unmarshaller<?>> typeHandlers;
	private final ReadableByteChannel sourceChannel;
	private final Set<Fixjure.Option> options;
	@Nullable
	private Charset preferredCharset = null;
	@Nullable
	private IdentityResolver identityResolver = null;

	protected FixtureSource(final ReadableByteChannel source) {
		sourceChannel = Preconditions.checkNotNull(source);
		typeHandlers = LinkedListMultimap.create();
		installDefaultHandlers();
		options = Sets.newEnumSet(DEFAULT_OPTIONS, Fixjure.Option.class);
	}

	/**
	 * Adds an option.
	 *
	 * @param opt option
	 */
	public final void addOption(final Fixjure.Option opt) {
		checkNotNull(opt);
		options.add(opt);
	}

	public final void withCharset(final String charset) {
		this.preferredCharset = Charset.forName(charset);
	}

	public void close() throws IOException {
		sourceChannel.close();
	}

	public final ImmutableSet<Fixjure.Option> getOptions() {
		return ImmutableSet.copyOf(options);
	}

	protected abstract Object createFixture(final FixtureType type);

	void setIdentityResolver(@Nullable final IdentityResolver resolver) {
		this.identityResolver = resolver;
	}

	private boolean canHandleIdentity(final Class<?> type, @Nullable final Object rawIdentityValue) {
		return identityResolver != null
				&& rawIdentityValue != null
				&& identityResolver.canHandleIdentity(type, rawIdentityValue);
	}

	private <T> Supplier<T> resolveIdentity(final Class<T> type, final Object rawIdentityValue) {
		if (canHandleIdentity(type, rawIdentityValue)) {
			assert identityResolver != null : "Don't attempt to resolve an id if the resolver is null!";
			return Suppliers.ofIdentity(identityResolver, type, rawIdentityValue);
		} else {
			return null;
		}
	}

	protected final ImmutableMultimap<Class<?>, Unmarshaller<?>> getTypeHandlers() {
		return ImmutableMultimap.copyOf(typeHandlers);
	}

	protected final ReadableByteChannel getSource() {
		return sourceChannel;
	}

	protected final Charset getCharset() {
		return preferredCharset;
	}

	protected final void installTypeHandler(final Unmarshaller<?> handler) {
		typeHandlers.put(handler.getReturnType(), handler);
		if (handler instanceof PrimitiveUnmarshaller) {
			final PrimitiveUnmarshaller primitiveUnmarshaller = (PrimitiveUnmarshaller) handler;
			typeHandlers.put(primitiveUnmarshaller.getPrimitiveType(), handler);
		}
	}

	protected final boolean isOptionEnabled(final Fixjure.Option option) {
		return options.contains(option);
	}

	public final Supplier<?> unmarshall(final Object rawValue, final FixtureType type) {
		final Unmarshaller<?> unmarshaller = findUnmarshaller(rawValue, type);
		return unmarshaller.unmarshall(this, rawValue, type);
	}

	/** {@inheritDoc} */
	protected final Unmarshaller<?> findUnmarshaller(final Object src, final FixtureType type) {
		final Class<?> cls = type.getType();

		if (cls.isInstance(src)) {
			return NoConversionUnmarshaller.newInstance(cls);
		}

		for (Class<?> keyClass = cls; keyClass != null; keyClass = keyClass.getSuperclass()) {
			for (final Unmarshaller<?> handler : getTypeHandlers().get(keyClass)) {
				if (handler.canUnmarshallObjectToType(src, type)) {
					return handler;
				}
			}
		}

		for (final Unmarshaller<?> handler : getTypeHandlers().get(Object.class)) {
			if (handler.canUnmarshallObjectToType(src, type)) {
				return handler;
			}
		}

		if (cls.isArray()) {
			for (final Unmarshaller<?> handler : getTypeHandlers().get(Object[].class)) {
				if (handler.canUnmarshallObjectToType(src, type)) {
					return handler;
				}
			}
		}

		//noinspection unchecked
		return new AbstractUnmarshaller(Object.class, cls) {
			public Supplier<?> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				return resolveIdentity(cls, src);
			}
		};
	}

	/**
	 * @param type object type
	 * @param value object value
	 * @return proxied object
	 */
	protected final Supplier<?> findValue(final FixtureType type, final Object value) {
		final Unmarshaller<?> handler = findUnmarshaller(value, type);
		return handler.unmarshall(this, value, type);
	}

	/** Install default fixture handlers. */
	private void installDefaultHandlers() {
		installTypeHandler(NoConversionUnmarshaller.newInstance(String.class));
		installTypeHandler(NoConversionUnmarshaller.newInstance(Boolean.class));
		installTypeHandler(NoConversionUnmarshaller.newInstance(Boolean.TYPE));

		for (final Class<?> t : NUMERIC_TYPES) {
			installTypeHandler(NoConversionUnmarshaller.newInstance(t));
		}

		final Unmarshaller<Byte> byteHandler = Unmarshallers.byteHandler();
		final Unmarshaller<Short> shortHandler = Unmarshallers.shortHandler();
		final Unmarshaller<Integer> intHandler = Unmarshallers.integerHandler();
		final Unmarshaller<Long> longHandler = Unmarshallers.longHandler();
		final Unmarshaller<Float> floatHandler = Unmarshallers.floatHandler();
		final Unmarshaller<Double> doubleHandler = Unmarshallers.doubleHandler();
		final Unmarshaller<BigInteger> bigintHandler = Unmarshallers.bigIntegerHandler();
		final Unmarshaller<BigDecimal> bigdecHandler = Unmarshallers.bigDecimalHandler();

		installTypeHandler(byteHandler);
		installTypeHandler(shortHandler);
		installTypeHandler(intHandler);
		installTypeHandler(longHandler);
		installTypeHandler(floatHandler);
		installTypeHandler(doubleHandler);
		installTypeHandler(bigintHandler);
		installTypeHandler(bigdecHandler);
		final ChainedUnmarshaller<Number> chainedHandler = new ChainedUnmarshaller<Number>(CharSequence.class, Number.class) {
			public Supplier<? extends Number> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				return Suppliers.of(Double.parseDouble(castSourceValue(CharSequence.class, source).toString()));
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

		installTypeHandler(Unmarshallers.stringBuilderHandler());
		installTypeHandler(Unmarshallers.javaDateHandler());
	}
}
