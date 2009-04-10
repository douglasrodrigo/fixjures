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
 * Fixture handler plugin which can intercept object deserialization and provide its
 * own behavior during fixture instantiation.
 * <p/>
 * Date: Mar 25, 2009
 * <p/>
 * Time: 11:26:54 AM
 *
 * @param <S> type of source object provided by FixtureSource
 * @param <R> type of object returned by this handler
 * @author Steve Reed
 */
public abstract class AbstractFixtureHandler<S,R> implements FixtureHandler<S,R>  {

	/**
	 * Evaluates a source object and desired type, returning true if the object can be passed to
	 * {@code apply(...)} and return a correct value.
	 *
	 * @param obj source object
	 * @param desiredType desired object type
	 * @return true if object can be transformed by this handler
	 */
	public boolean canDeserialize(final Object obj, final Class<?> desiredType) {
		return getReturnType().isAssignableFrom(desiredType)
				  && (obj == null || getSourceType().isAssignableFrom(obj.getClass()));
	}

}
