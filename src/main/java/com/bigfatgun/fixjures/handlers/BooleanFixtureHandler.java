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

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureHandler;

/**
 * Handles booleans.
 *
 * @author Steve Reed
 */
public final class BooleanFixtureHandler implements FixtureHandler<Object, Boolean> {

	/**
	 * Returns {@code Boolean.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<Boolean> getType() {
		return Boolean.class;
	}

	/**
	 * Converts the given object to a boolean. TODO add support for converting a number to a boolean, or perhaps
	 * testing for nulls/non-nulls.
	 * <p>
	 * {@inheritDoc}
	 */
	public Boolean deserialize(final Class desiredType, final Object rawValue, final String name) {
		if (desiredType.isAssignableFrom(rawValue.getClass())) {
			//noinspection unchecked
			return (Boolean) rawValue;
		} else {
			Fixjure.LOGGER.warning(String.format("Type mismatch, stubbing %s to return null.", name));
			return null;
		}
	}
}
