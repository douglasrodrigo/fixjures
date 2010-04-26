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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Abstract base class for other source factories that can be easily implemented on top of a {@link
 * java.nio.channels.ReadableByteChannel}.
 */
public abstract class AbstractSourceFactory implements SourceFactory {

	private final Strategies.SourceStrategy dataSourceStrategy;

	protected AbstractSourceFactory(final Strategies.SourceStrategy dataSourceStrategy) {
		checkNotNull(dataSourceStrategy);
		this.dataSourceStrategy = dataSourceStrategy;
	}

	/**
	 * Uses the data source strategy to find fixture source data, and wraps any {@code IOException}s with a {@link
	 * com.bigfatgun.fixjures.FixtureException}.
	 *
	 * @param fixtureObjectType fixture object type
	 * @param fixtureId object id
	 * @return source data channel
	 */
	protected ReadableByteChannel loadFixtureDataSource(final Class<?> fixtureObjectType, final String fixtureId) {
		checkNotNull(fixtureObjectType);
		checkNotNull(fixtureId);

		try {
			return dataSourceStrategy.findStream(fixtureObjectType, fixtureId);
		} catch (IOException e) {
			throw FixtureException.convert(e);
		}
	}
}
