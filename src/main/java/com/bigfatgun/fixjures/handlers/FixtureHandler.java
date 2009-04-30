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
 * Handles conversion of source data to destination data.
 *
 * @param <S> source type
 * @param <R> return type
 * @author Steve Reed
 */
public interface FixtureHandler<S,R> {

	/**
	 * Evaluates a source object and desired type, returning true if the object can be passed to
	 * {@code apply(...)} and return a correct value.
	 *
	 * @param obj source object
	 * @param desiredType desired object type
	 * @return true if object can be transformed by this handler
	 */
	boolean canDeserialize(Object obj, Class<?> desiredType);

	/**
	 * Returns the type of object created by this handler.
	 * @return the type of object created by this handler
	 */
	Class<R> getReturnType();

	/**
	 * Returns the type of object required by this handler.
	 * @return the type of object required by this handler
	 */
	Class<S> getSourceType();

	/**
	 * Convers the source object to the return type object.
	 *
	 * @param helper fixture handler helper
	 * @param s source object
	 * @return return object
	 */
   R apply(HandlerHelper helper, S s);
}
