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
 * Handles {@code Number}s.
 *
 * @author Steve Reed
 */
public abstract class NumberFixtureHandler<NumericType extends Number> extends AbstractFixtureHandler<Number, NumericType> {

	/**
	 * Abstract method provided by subclasses to provide the primitive type.
	 *
	 * @return numeric primitive type
	 */
	protected abstract Class<NumericType> getPrimitiveType();

	/**
	 * Returns {@code Number}.
	 * <p>
	 * {@inheritDoc}
	 */
	public final Class<Number> getSourceType() {
		return Number.class;
	}

	/**
	 * Returns true if the desired type is the correct source type or primitive type.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public final boolean canDeserialize(final Object obj, final Class<?> desiredType) {
		return super.canDeserialize(obj, desiredType) || getPrimitiveType().equals(desiredType);
	}
}
