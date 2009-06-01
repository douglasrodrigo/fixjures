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

/**
 * Converts a {@code String} to a {@code StringBuilder}.
 *
 * @author Steve Reed
 */
final class StringBuilderFixtureHandler extends AbstractFixtureHandler<CharSequence, StringBuilder> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<CharSequence> getSourceType() {
		return CharSequence.class;
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
	public StringBuilder apply(final HandlerHelper helper, final CharSequence s) {
		return new StringBuilder(s);
	}
}