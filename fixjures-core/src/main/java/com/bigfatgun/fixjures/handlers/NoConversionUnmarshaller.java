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

import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.Suppliers;
import com.google.common.base.Supplier;

/**
 * Simple object pass-through for when no conversion is necessary.
 *
 * @author Steve Reed
 */
public final class NoConversionUnmarshaller<T> extends AbstractUnmarshaller<T> {

	/**
	 * Static factory method for better type inference.
	 *
	 * @param cls type
	 * @param <T> type
	 * @return new handler of type
	 */
	public static <T> NoConversionUnmarshaller<T> newInstance(final Class<T> cls) {
		return new NoConversionUnmarshaller<T>(cls);
	}

	/**
	 * Creates a new handler of a given type.
	 *
	 * @param type type
	 */
	private NoConversionUnmarshaller(final Class<T> type) {
		super(type, type);
	}

	public Supplier<? extends T> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
		return Suppliers.of(castSourceValue(getReturnType(), source));
	}
}
