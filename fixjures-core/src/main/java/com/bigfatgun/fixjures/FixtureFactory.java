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

import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.handlers.Unmarshaller;
import com.bigfatgun.fixjures.serializable.ObjectInputStreamSource;
import com.google.common.base.Function;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Throwables;
import com.google.common.collect.ComputationException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * FixjureFactory is a utility helpful when creating many fixtures for it can easily produce many fixture sources based
 * on a single configuration by utilizing monolithic {@link com.bigfatgun.fixjures.SourceFactory}s, or by combining
 * {@link com.bigfatgun.fixjures.Strategies.SourceStrategy}s and
 * {@link com.bigfatgun.fixjures.Strategies.ResourceNameStrategy}s.
 *
 * @author Steve Reed
 */
public final class FixtureFactory implements IdentityResolver {

	/**
	 * Creates a new factory that will use the given source factory.
	 *
	 * @param sourceFactory source factory
	 * @return new fixture factory
	 */
	public static FixtureFactory newFactory(final SourceFactory sourceFactory) {
		checkNotNull(sourceFactory);
		return new FixtureFactory(sourceFactory);
	}

	/**
	 * Creates a new factory that will use {@link com.bigfatgun.fixjures.serializable.ObjectInputStreamSource}s backed by
	 * data provided by the given {@link com.bigfatgun.fixjures.Strategies.SourceStrategy}.
	 *
	 * @param sourceStrategy strategy to use to find source data
	 * @return new fixture factory
	 */
	public static FixtureFactory newObjectInputStreamFactory(final Strategies.SourceStrategy sourceStrategy) {
		checkNotNull(sourceStrategy);
		return new FixtureFactory(new SourceFactory() {
			public FixtureSource newInstance(final Class<?> type, final String name) {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				try {
					return ObjectInputStreamSource.newObjectInputStream(sourceStrategy.findStream(type, name));
				} catch (IOException e) {
					throw convert(e);
				}
			}
		});
	}

	private final SourceFactory srcFactory;
	private final Set<Fixjure.Option> options;
	private final Set<Unmarshaller<?>> handlers;
	private final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> objectCache;

	/**
	 * Creates a new fixture factory and initializes the fixture object compute map.
	 *
	 * @param sourceFactory source factory
	 */
	private FixtureFactory(final SourceFactory sourceFactory) {
		assert sourceFactory != null : "Source factory cannot be null.";

		srcFactory = sourceFactory;
		options = EnumSet.noneOf(Fixjure.Option.class);
		handlers = Sets.newHashSet();

		objectCache = new MapMaker()
				.makeComputingMap(new Function<Class<?>, ConcurrentMap<String, Object>>() {
					public ConcurrentMap<String, Object> apply(final Class<?> type) {
                        checkNotNull(type);

                        return new MapMaker()
                                .softValues()
                                .makeComputingMap(new Function<String, Object>() {
                                    public Object apply(final String name) {
                                        checkNotNull(name);

                                        Fixjure.SourcedFixtureBuilder<?> fixtureBuilder = Fixjure.of(type).from(srcFactory.newInstance(type, name)).withOptions(ImmutableSet.copyOf(options));
                                        for (final Unmarshaller<?> handler : handlers) {
                                            fixtureBuilder = fixtureBuilder.with(handler);
                                        }
                                        fixtureBuilder = fixtureBuilder.resolveIdsWith(FixtureFactory.this);
                                        return fixtureBuilder.create();
                                    }
                                });
                    }
				});
	}

	/**
	 * Returns true if the raw value is a string.
	 * <p/>
	 * {@inheritDoc}
	 */
	public boolean canHandleIdentity(final Class<?> requiredType, final Object rawIdentityValue) {
		return !requiredType.isAssignableFrom(CharSequence.class) && rawIdentityValue instanceof String;
	}

	/**
	 * Converts the raw identity value into a string.
	 *
	 * @param rawIdentityValue raw identity value
	 * @return identity as string
	 */
	public String coerceIdentity(final Object rawIdentityValue) {
		return (String) rawIdentityValue;
	}

	/**
	 * Resolves the given object by type and name.
	 *
	 * @param requiredType object type
	 * @param id object name
	 * @return object of type identified by id, null if not found
	 */
	public <T> T resolve(final Class<T> requiredType, final String id) {
		return createFixture(requiredType, id);
	}

	/**
	 * Enables the given option. By enabling this option it will be passed into every fixture builder created by this
	 * factory.
	 *
	 * @param option option to enable, not null
	 * @return this
	 */
	public FixtureFactory enableOption(final Fixjure.Option option) {
        checkNotNull(option);
        options.add(option);
        return this;
    }

	/**
	 * Disables the given option.
	 *
	 * @param option option to disable, not null
	 * @return this
	 */
	public FixtureFactory disableOption(final Fixjure.Option option) {
        checkNotNull(option);
        options.remove(option);
        return this;
    }

	/**
	 * Adds a fixture handler that will be passed into every fixture builder created by this factory.
	 *
	 * @param handler handler to add, not null
	 * @return this
	 */
	public FixtureFactory addFixtureHandler(final Unmarshaller<?> handler) {
        checkNotNull(handler);
        handlers.add(handler);
        return this;
    }

	/**
	 * Removes a fixture handler.
	 *
	 * @param handler handler to remove, not null
	 * @return this
	 */
	public FixtureFactory removeFixtureHandler(final Unmarshaller<?> handler) {
        checkNotNull(handler);
        handlers.remove(handler);
        return this;
    }

	/**
	 * Creates a fixture object of the given type. The name is used by underlying source factories or resource name
	 * strategies to locate the fixture object source.
	 *
	 * @param type fixture object type
	 * @param <T> fixture object type
	 * @param name fixture object name or id
	 * @return new fixture object
	 */
	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public <T> T createFixture(final Class<T> type, final String name) {
        checkNotNull(type);
        checkNotNull(name);

        try {
            final ConcurrentMap<String, Object> classMap = objectCache.get(type);
            final Object obj = classMap.get(name);
            return type.cast(obj);
        } catch (ComputationException e) {
            throw convert(Throwables.getRootCause(e));
        }
    }

	/** Clears the fixture object cache. */
	public void expireCache() {
		objectCache.clear();
	}

	@SuppressWarnings({"unchecked"})
	public <T> T uncache(final Class<T> cls, final String identifier) {
		return (T) objectCache.get(cls).remove(identifier);
	}

	@SuppressWarnings({"unchecked"})
	public <T> T cache(final Class<T> cls, final T object, final String identifier) {
		return (T) objectCache.get(cls).put(identifier, object);
	}
}
