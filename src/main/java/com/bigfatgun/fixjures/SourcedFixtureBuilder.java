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

import java.io.IOException;

import com.google.common.collect.ImmutableMap;

/**
 * A "sourced" fixture builder, meaning it has at least the necessary state to begin
 * reading fixtures from some type of data.
 *
 * @param <T> fixture object type
 * @param <SourceType> fixture source type
 * @author Steve Reed
 */
public abstract class SourcedFixtureBuilder<T, SourceType extends FixtureSource> extends FixtureBuilder<T> {

	/**
	 * Map of desired value type to fixture handler.
	 */
	private ImmutableMap<Class, FixtureHandler> handlers;

	/**
	 * Fixture data source.
	 */
	private final SourceType fixtureSource;

	/**
	 * Protected constructor that stores the given builder's state.
	 *
	 * @param builder builder to copy
	 * @param source fixture data source
	 */
	protected SourcedFixtureBuilder(final FixtureBuilder<T> builder, final SourceType source) {
		super(builder.getType());
		fixtureSource = source;
		handlers = ImmutableMap.of();
	}

	/**
	 * @return fixture data source
	 */
	protected final SourceType getSource() {
		return fixtureSource;
	}

	/**
	 * Creates a new fixture object. This methid is implemented by subclasses which
	 * have an explicit knowledge of the fixture source format, such as the
	 * {@link com.bigfatgun.fixjures.json.JSONSource}.
	 *
	 * @param handlers fixture handlers
	 * @return new object from fixture source
	 * @throws Exception if there is any sort of error reading the next object
	 */
	protected abstract T createFixtureObject(final ImmutableMap<Class, FixtureHandler> handlers) throws Exception;

	/**
	 * Adds a fixture handler to this builder.
	 *
	 * @param handler handler to add
	 * @return this
	 */
	public SourcedFixtureBuilder<T, SourceType> with(final FixtureHandler handler) {
		handlers = ImmutableMap.<Class, FixtureHandler>builder().putAll(handlers).put(handler.getType(), handler).build();
		return this;
	}

	/**
	 * Strongly-typed fixture creation method. This method forwards calls to
	 * {@link SourcedFixtureBuilder#createFixtureObject(com.google.common.collect.ImmutableMap)} and then attempts to
	 * cast the object into the required type.
	 *
	 * @return new fixture object, or null if the object could not be created
	 */
	public final T create() {
		try {
			return createFixtureObject(handlers);
		} catch (Exception e) {
			Fixjure.LOGGER.warning(e.getMessage());
			return null;
		} finally {
//			if (!(fixtureSource instanceof FixtureStream)) {
				try {
					fixtureSource.close();
				} catch (IOException e) {
					Fixjure.LOGGER.warning(String.format("Source close error: %s", e.getMessage()));
				}
//			}
		}
	}
}
