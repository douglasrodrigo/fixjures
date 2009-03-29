/*
 * Copyright (C) 2009 bigfatgun.com
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

/**
 * Your basic fixture builder that provides the simplest implementation of
 * the {@code from} method, which passes the type info into a fixture source
 * in order to create a {@link SourcedFixtureBuilder}.
 *
 * @param <T> fixture object type
 */
public class FixtureBuilder<T> {

	/** Fixture object type. */
	private final Class<T> clazz;

	/**
	 * Instantiates a new fixture builder.
	 *
	 * @param cls fixture object type
	 */
	/* package */ FixtureBuilder(final Class<T> cls) {
		clazz = cls;
	}

	/**
	 * Uses the given {@code FixtureSource} to convert this builder into
	 * a {@code SourcedFixtureBuilder}.
	 *
	 * @param source fixture source, could be JSON or otherwise
	 * @return sourced fixture builder
	 */
	public SourcedFixtureBuilder<T, ? extends FixtureSource> from(final FixtureSource source) {
		return source.build(this);
	}

	/**
	 * @return fixture object type
	 */
	public final Class<T> getType() {
		return clazz;
	}
}
