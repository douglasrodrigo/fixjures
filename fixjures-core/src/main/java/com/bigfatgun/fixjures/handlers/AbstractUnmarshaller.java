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

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureType;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Supplier;

/**
 * Fixture handler plugin which can intercept object deserialization and provide its
 * own behavior during fixture instantiation.
 */
public abstract class AbstractUnmarshaller<T> implements Unmarshaller<T> {

	private final Class<?> sourceType;
	private final Class<T> returnType;

	protected AbstractUnmarshaller(final Class<?> sourceType, final Class<T> returnType) {
		this.sourceType = checkNotNull(sourceType);
		this.returnType = checkNotNull(returnType);
	}

	protected final <T> T castSourceValue(final Class<T> type, final Object object) {
		try {
			return sourceType.asSubclass(type).cast(object);
		} catch (ClassCastException e) {
			throw new FixtureException("Could not cast " + object + " (" + ((object == null) ? "" : object.getClass().getName()) + ") to " + type + ".");
		}
	}

	protected final Class<?> getSourceType() {
		return sourceType;
	}

	@Override
	public final Class<T> getReturnType() {
		return returnType;
	}

	/**
	 * Evaluates a source object and desired type, returning true if the object can be passed to
	 * {@code apply(...)} and return a correct value.
	 *
	 * @param obj source object
	 * @param desiredType desired object type
	 * @return true if object can be transformed by this handler
	 */
	@Override
	public boolean canUnmarshallObjectToType(final Object obj, final FixtureType desiredType) {
		return getReturnType().isAssignableFrom(desiredType.getType())
				  && (obj == null || sourceType.isAssignableFrom(obj.getClass()));
	}

	protected final Supplier<?> help(final UnmarshallingContext unmarshallingContext, final Object source, final FixtureType type) {
		return unmarshallingContext.unmarshall(source, type);
	}
}
