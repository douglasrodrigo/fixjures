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

/**
 * Abstract fixture source which provides a no-op implementation of
 * {@code java.io.Closeable.close()}.
 */
public abstract class FixtureSource implements Closeable {

	/**
	 * Converts the given builder into a "sourced" fixture builder.
	 *
	 * @param <T> fixture object type
	 * @param builder the builder to convert
	 * @return sourced fixture builder
	 */
	public abstract <T> SourcedFixtureBuilder<T, ? extends FixtureSource> build(FixtureBuilder<T> builder);

	/**
	 * No-op.
	 * <p>
	 * {@inheritDoc}
	 */
	public void close() throws IOException {
		// nothing to do, override this
	}
}
