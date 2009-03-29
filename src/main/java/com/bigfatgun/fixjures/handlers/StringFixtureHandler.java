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
package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureHandler;

/**
 * Handles strings.
 */
public final class StringFixtureHandler implements FixtureHandler<Object, String> {

	/**
	 * Returns {@code String.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<String> getType() {
		return String.class;
	}

	/**
	 * Converts the object to a string only if it's assignable from the {@code desiredType}.
	 * <p>
	 * {@inheritDoc}
	 */
	public String deserialize(final Class desiredType, final Object rawValue, final String name) {
		if (desiredType.isAssignableFrom(rawValue.getClass())) {
			//noinspection unchecked
			return String.valueOf(rawValue);
		} else {
			Fixjure.LOGGER.warning(String.format("Type mismatch, stubbing %s to return null.", name));
			return null;
		}
	}
}
