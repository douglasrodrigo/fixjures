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

/**
 * Handles {@code Number}s.
 */
public final class NumberFixtureHandler implements FixtureHandler<Number, Number> {

	/**
	 * Returns {@code Number.class}.
	 * <p>
	 * {@inheritDoc}
	 */
	public Class<Number> getType() {
		return Number.class;
	}

	/**
	 * Converts the given number to the desired type.
	 * <p>
	 * {@inheritDoc}
	 */
	public Number deserialize(final Class desiredType, final Number rawValue, final String name) {
		if (Byte.class.isAssignableFrom(desiredType) || Byte.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.byteValue();
		} else if (Short.class.isAssignableFrom(desiredType) || Short.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.shortValue();
		} else if (Integer.class.isAssignableFrom(desiredType) || Integer.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.intValue();
		} else if (Long.class.isAssignableFrom(desiredType) || Long.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.longValue();
		} else if (Float.class.isAssignableFrom(desiredType) || Float.TYPE.isAssignableFrom(desiredType)) {
			return rawValue.floatValue();
		} else {
			return rawValue.doubleValue();
		}
	}
}
