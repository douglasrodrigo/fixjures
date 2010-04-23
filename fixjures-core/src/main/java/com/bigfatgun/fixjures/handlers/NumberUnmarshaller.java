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
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Handles {@code Number}s.
 *
 * @author Steve Reed
 */
abstract class NumberUnmarshaller<T extends Number> extends AbstractUnmarshaller<T> implements PrimitiveUnmarshaller<T> {

	private final Class<T> primitiveType;

	protected NumberUnmarshaller(final Class<T> returnType, final Class<T> primitiveType) {
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
	 * <p/>
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canUnmarshallObjectToType(final Object obj, final FixtureType desiredTypeDef) {
		final Class<?> desiredType = desiredTypeDef.getType();
		return (obj == null || Number.class.isAssignableFrom(obj.getClass()))
				&& (getReturnType().equals(desiredType) || getPrimitiveType().equals(desiredType));
	}

	public final Supplier<T> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
		return Suppliers.ofInstance(narrowNumericValue(castSourceValue(Number.class, source)));
	}

	protected abstract T narrowNumericValue(final Number number);
}
