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

import com.bigfatgun.fixjures.FixtureHandler;
import com.google.common.base.Nullable;

/**
 * Converts a {@code String} to a {@code StringBuilder}.
 *
 * @author Steve Reed
 */
public final class StringBuilderFixtureHandler extends FixtureHandler<String, StringBuilder> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<String> getSourceType() {
		return String.class;
	}

	/**
	 * Returns {@code StringBuilder.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<StringBuilder> getReturnType() {
		return StringBuilder.class;
	}

	/**
	 * Wraps the object in a {@code StringBuilder}.
	 * <p>
	 * {@inheritDoc}
	 */
	public StringBuilder apply(@Nullable final String s) {
		return new StringBuilder(s);
	}
}
