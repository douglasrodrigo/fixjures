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

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import com.bigfatgun.fixjures.handlers.FixtureHandler;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.base.Function;
import com.google.common.base.Nullable;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

/**
 * FixjureFactory is a utility helpful when creating many fixtures for it can easily produce many
 * fixture sources based on a single configuration by utilizing monolithic
 * {@link com.bigfatgun.fixjures.SourceFactory}s, or by combinding
 * {@link com.bigfatgun.fixjures.Strategies.SourceStrategy}s and
 * {@link com.bigfatgun.fixjures.Strategies.ResourceNameStrategy}s.
 *
 * @author Steve Reed
 */
public final class FixjureFactory {

	/**
	 * Creates a new factory that will use the given source factory.
	 *
	 * @param sourceFactory source factory
	 * @return new fixture factory
	 */
	public static FixjureFactory newFactory(final SourceFactory sourceFactory) {
		if (sourceFactory == null) {
			throw new NullPointerException("sourceFactory");
		}
		return new FixjureFactory(sourceFactory);
	}

	/**
	 * Creates a new factory that will use {@link com.bigfatgun.fixjures.json.JSONSource}s backed by
	 * data provided by the given {@link com.bigfatgun.fixjures.Strategies.SourceStrategy}.
	 *
	 * @param sourceStrategy strategy to use to find json source
	 * @return new fixture factory
	 */
	public static FixjureFactory newJsonFactory(final Strategies.SourceStrategy sourceStrategy) {
		if (sourceStrategy == null) {
			throw new NullPointerException("sourceStrategy");
		}
		return new FixjureFactory(new SourceFactory() {
			public FixtureSource newInstance(final Class<?> type, final String name) {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				try {
					return JSONSource.newJsonStream(sourceStrategy.findStream(type, name));
				} catch (IOException e) {
					throw new FixtureException(e);
				}
			}
		});
	}

	/**
	 * Source factory.
	 */
	private final SourceFactory srcFactory;

	/**
	 * Set of options to use when creating fixtures.
	 */
	private final Set<Fixjure.Option> options;

	/**
	 * Set of fixture handlers to use.
	 */
	private final Set<FixtureHandler<?, ?>> handlers;

	/**
	 * Compute map that stores a map of object name to object for every fixture object type.
	 */
	private final ConcurrentMap<Class<?>, ConcurrentMap<String, Object>> objectCache;

	/**
	 * Creates a new fixture factory and initializes the fixture object compute map.
	 *
	 * @param sourceFactory source factory
	 */
	private FixjureFactory(final SourceFactory sourceFactory) {
		if (sourceFactory == null) {
			throw new NullPointerException("sourceFactory");
		}
		srcFactory = sourceFactory;
		options = EnumSet.noneOf(Fixjure.Option.class);
		handlers = Sets.newHashSet();
		objectCache = new MapMaker()
				  .expiration(10, TimeUnit.MINUTES)
				  .makeComputingMap(new Function<Class<?>, ConcurrentMap<String, Object>>() {
			public ConcurrentMap<String, Object> apply(@Nullable final Class<?> type) {
				assert type != null : "Type cannot be null.";
				return new MapMaker()
						  .expiration(1, TimeUnit.MINUTES)
						  .weakValues()
						  .makeComputingMap(new Function<String, Object>() {
							  public Object apply(@Nullable final String name) {
								  assert name != null : "Name cannot be null.";

								  Fixjure.SourcedFixtureBuilder<?> fixtureBuilder = Fixjure.of(type).from(srcFactory.newInstance(type, name)).withOptions(options);
								  for (final FixtureHandler<?, ?> handler : handlers) {
									  fixtureBuilder = fixtureBuilder.with(handler);
								  }
								  return fixtureBuilder.create();
							  }
						  });
			}
		});
	}

	/**
	 * Enables the given option. By enabling this option it will be passed into every fixture builder created
	 * by this factory.
	 *
	 * @param option option to enable, not null
	 */
	public void enableOption(final Fixjure.Option option) {
		options.add(option);
	}

	/**
	 * Disables the given option.
	 * @param option option to disable, not null
	 */
	public void disableOption(final Fixjure.Option option) {
		options.remove(option);
	}

	/**
	 * Adds a fixture handler that will be passed into every fixture builder created by this factory.
	 *
	 * @param handler handler to add, not null
	 */
	public void addFixtureHandler(final FixtureHandler<?, ?> handler) {
		handlers.add(handler);
	}

	/**
	 * Removes a fixture handler.
	 *
	 * @param handler handler to remove, not null
	 */
	public void removeFixtureHandler(final FixtureHandler<?, ?> handler) {
		handlers.remove(handler);
	}

	/**
	 * Creates a fixture object of the given type. The name is used by underlying source factories or resource
	 * name strategies to locate the fixture object source.
	 *
	 * @param type fixture object type
	 * @param <T> fixture object type
	 * @param name fixture object name or id
	 * @return new fixture object
	 */
	public <T> T createFixture(final Class<T> type, final String name) {
		if (type == null) {
			throw new NullPointerException("type");
		} else if (name == null) {
			throw new NullPointerException("name");
		}

		try {
			return type.cast(objectCache.get(type).get(name));
		} catch (ComputationException e) {
			if (e.getCause() instanceof FixtureException) {
				throw ((FixtureException) e.getCause());
			} else {
				throw new FixtureException(e.getCause());
			}
		}
	}

	/**
	 * Clears the fixture object cache.
	 */
	public void expireCache() {
		objectCache.clear();
	}
}
