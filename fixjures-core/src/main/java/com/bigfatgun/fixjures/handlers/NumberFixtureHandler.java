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
import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;

/**
 * Handles {@code Number}s.
 *
 * @author Steve Reed
 */
abstract class NumberFixtureHandler<T extends Number> extends AbstractFixtureHandler<T> implements PrimitiveHandler<T> {

	private final Class<T> primitiveType;

	protected NumberFixtureHandler(final Class<T> returnType, final Class<T> primitiveType) {
		super(Number.class, returnType);
		this.primitiveType = primitiveType;
	}

	/**
	 * Abstract method provided by subclasses to provide the primitive type.
	 *
	 * @return numeric primitive type
	 */
	public final Class<T> getPrimitiveType() {
		return primitiveType;
	}

	/**
	 * Returns true if the desired type is the correct source type or primitive type.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canDeserialize(final Object obj, final Class<?> desiredType) {
		return (obj == null || Number.class.isAssignableFrom(obj.getClass()))
				  && (getReturnType().equals(desiredType) || getPrimitiveType().equals(desiredType));
	}

	@Override
	public final ValueProvider<T> apply(final HandlerHelper helper, final FixtureType typeDef, final Object source) {
		return ValueProviders.of(narrowNumericValue(castSourceValue(Number.class, source)));
	}

	protected abstract T narrowNumericValue(final Number number);
}
