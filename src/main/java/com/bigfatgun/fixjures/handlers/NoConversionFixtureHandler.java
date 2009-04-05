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
package com.bigfatgun.fixjures.handlers;

import com.google.common.base.Nullable;

/**
 * Simple object pass-through for when no conversion is necessary.
 *
 * @author Steve Reed
 */
public final class NoConversionFixtureHandler<T> extends FixtureHandler<T, T> {

	/**
	 * Static factory method for better type inference.
	 *
	 * @param cls type
	 * @param <T> type
	 * @return new handler of type
	 */
	public static <T> NoConversionFixtureHandler<T> newInstance(final Class<T> cls) {
		return new NoConversionFixtureHandler<T>(cls);
	}

	/** The type. */
	private final Class<T> cls;

	/**
	 * Creates a new handler of a given type.
	 *
	 * @param type type
	 */
	private NoConversionFixtureHandler(final Class<T> type) {
		super();
		cls = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getSourceType() {
		return cls;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> getReturnType() {
		return cls;
	}

	/**
	 * {@inheritDoc}
	 */
	public T apply(@Nullable final T t) {
		return t;
	}
}
