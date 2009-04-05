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

import com.bigfatgun.fixjures.handlers.FixtureHandler;

/**
 * A "sourced" fixture builder, meaning it has at least the necessary state to begin
 * reading fixtures from some type of data.
 *
 * @param <T> fixture object type
 * @param <S> fixture source type
 * @author Steve Reed
 */
public abstract class SourcedFixtureBuilder<T, S extends FixtureSource> extends FixtureBuilder<T> {

	/**
	 * Fixture data source.
	 */
	private final S fixtureSource;

	/**
	 * Protected constructor that stores the given builder's state.
	 *
	 * @param builder builder to copy
	 * @param source fixture data source
	 */
	protected SourcedFixtureBuilder(final FixtureBuilder<T> builder, final S source) {
		super(builder);
		fixtureSource = source;
	}

	/**
	 * Adds a fixture handler to this builder.
	 *
	 * @param handler handler to add
	 * @return this
	 */
	public final SourcedFixtureBuilder<T, S> with(final FixtureHandler handler) {
		getSource().installRequiredTypeHandler(handler);
		return this;
	}

	/**
	 * Strongly-typed fixture creation method. This method forwards calls to
	 * {@link SourcedFixtureBuilder#createFixtureObject()} and then attempts to
	 * cast the object into the required type.
	 *
	 * @return new fixture object, or null if the object could not be created
	 */
	public final T create() {
		try {
			return createFixtureObject();
		} finally {
			try {
				fixtureSource.close();
			} catch (IOException e) {
				Fixjure.LOGGER.warning(String.format("Source close error: %s", e.getMessage()));
			}
		}
	}

	/**
	 * Adds options.
	 * @param opts options
	 * @return this
	 */
	public final SourcedFixtureBuilder<T, S> withOptions(final Fixjure.Option... opts) {
		for (final Fixjure.Option opt : opts) {
			getSource().addOption(opt);
		}
		return this;
	}

	/**
	 * Creates a new fixture object. This methid is implemented by subclasses which
	 * have an explicit knowledge of the fixture source format, such as the
	 * {@link com.bigfatgun.fixjures.json.JSONSource}.
	 *
	 * @return new object from fixture source
	 */
	protected abstract T createFixtureObject();

	/**
	 * @return fixture data source
	 */
	protected final S getSource() {
		return fixtureSource;
	}
}
