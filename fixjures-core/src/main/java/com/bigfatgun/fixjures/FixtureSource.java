/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigfatgun.fixjures;

import com.bigfatgun.fixjures.handlers.*;
import com.bigfatgun.fixjures.proxy.ObjectProxyData;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

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

	private Charset preferredCharset = null;
	private IdentityResolver identityResolver = null;

	protected FixtureSource(final ReadableByteChannel source) {
		sourceChannel = Preconditions.checkNotNull(source);
		typeHandlers = LinkedListMultimap.create();
		installDefaultHandlers();
		options = Sets.newEnumSet(DEFAULT_OPTIONS, Fixjure.Option.class);
	}

	public final void addOptions(final Fixjure.Option opt, final Fixjure.Option... others) {
		addOption(opt);
		for (final Fixjure.Option other : others) {
			addOption(other);
		}
	}

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

    void setIdentityResolver(final IdentityResolver resolver) {
		this.identityResolver = resolver;
	}

	private boolean canHandleIdentity(final Class<?> type, final Object rawIdentityValue) {
		assert type !=  null : "Type cannot be null!";

		return identityResolver != null
				&& rawIdentityValue != null
				&& identityResolver.canHandleIdentity(type, rawIdentityValue);
	}

	private <T> Supplier<T> resolveIdentity(final Class<T> type, final Object rawIdentityValue) {
		assert type != null : "Type cannot be null!";

		if (canHandleIdentity(type, rawIdentityValue)) {
			assert identityResolver != null : "Don't attempt to resolve an id if the resolver is null!";
			return Resolvers.ofIdentity(identityResolver, type, rawIdentityValue);
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

	public final Supplier<?> unmarshall(final Object rawValue, final FixtureType type) {
		final Unmarshaller<?> unmarshaller = findUnmarshaller(rawValue, type);
		return unmarshaller.unmarshall(this, rawValue, type);
	}

	/** {@inheritDoc} */
	@SuppressWarnings({"unchecked"})
	protected final Unmarshaller<?> findUnmarshaller(final Object src, final FixtureType type) {
		final Class<?> cls = type.getType();

		assert cls != null : "Type class cannot be null.";

		if (type.getParams().isEmpty() && cls.isInstance(src)) {
			return NoConversionUnmarshaller.newInstance(cls);
		}

		for (final Unmarshaller<?> handler : getUnmarshallerCandidates(cls)) {
			if (handler.canUnmarshallObjectToType(src, type)) {
				return handler;
			}
		}

		return new AbstractUnmarshaller(Object.class, cls) {
			public Supplier<?> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				return resolveIdentity(cls, src);
			}
		};
	}

	private Iterable<Unmarshaller<?>> getUnmarshallerCandidates(final Class<?> cls) {
		assert cls != null : "Class cannot be null.";

		final ImmutableList.Builder<Unmarshaller<?>> candidateBuilder = ImmutableList.builder();

		for (Class<?> keyClass = cls; keyClass != null; keyClass = keyClass.getSuperclass()) {
			candidateBuilder.addAll(getTypeHandlers().get(keyClass));
		}

		candidateBuilder.addAll(getTypeHandlers().get(Object.class));

		if (cls.isArray()) {
			candidateBuilder.addAll(getTypeHandlers().get(Object[].class));
		}

		return candidateBuilder.build();
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

        final Unmarshaller<Boolean> boolHandler = Unmarshallers.boolHandler();
		final Unmarshaller<Byte> byteHandler = Unmarshallers.byteHandler();
		final Unmarshaller<Short> shortHandler = Unmarshallers.shortHandler();
		final Unmarshaller<Integer> intHandler = Unmarshallers.integerHandler();
		final Unmarshaller<Long> longHandler = Unmarshallers.longHandler();
		final Unmarshaller<Float> floatHandler = Unmarshallers.floatHandler();
		final Unmarshaller<Double> doubleHandler = Unmarshallers.doubleHandler();
		final Unmarshaller<BigInteger> bigintHandler = Unmarshallers.bigIntegerHandler();
		final Unmarshaller<BigDecimal> bigdecHandler = Unmarshallers.bigDecimalHandler();

        installTypeHandler(boolHandler);
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
				return Suppliers.ofInstance(Double.parseDouble(castSourceValue(CharSequence.class, source).toString()));
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

		installTypeHandler(Unmarshallers.newArrayHandler());
		installTypeHandler(Unmarshallers.newListHandler());
		installTypeHandler(Unmarshallers.newMapHandler());
		installTypeHandler(Unmarshallers.newMultisetHandler());
		installTypeHandler(Unmarshallers.newSetHandler());
		installTypeHandler(Unmarshallers.newCollectionHandler());
		installTypeHandler(Unmarshallers.newObjectProxyHandler());

		final ChainedUnmarshaller<ObjectProxyData> chainedMapHandler = new ChainedUnmarshaller<ObjectProxyData>(Map.class, ObjectProxyData.class) {
			@Override
			public Supplier<ObjectProxyData> unmarshall(UnmarshallingContext helper, Object source, FixtureType typeDef) {
				return Suppliers.ofInstance(new ObjectProxyData(castSourceValue(Map.class, source)));
			}
		};

		installTypeHandler(chainedMapHandler.link(Unmarshallers.newObjectProxyHandler()));

		installTypeHandler(new AbstractUnmarshaller<String>(Object.class, String.class) {
			@Override
			public Supplier<String> unmarshall(UnmarshallingContext helper, Object source, FixtureType typeDef) {
				return Suppliers.ofInstance(String.valueOf(source));
			}
		});

        installTypeHandler(new AbstractUnmarshaller<Enum>(String.class, Enum.class) {
            @SuppressWarnings({"unchecked"})
            @Override
            public Supplier<Enum> unmarshall(UnmarshallingContext ctx, Object source, FixtureType typeDef) {
                Class<? extends Enum> enumCls = (Class<? extends Enum>) typeDef.getType();
                return Suppliers.ofInstance(Enum.valueOf(enumCls, String.valueOf(source)));
            }
        });
	}
}
