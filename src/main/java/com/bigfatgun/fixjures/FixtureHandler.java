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
package com.bigfatgun.fixjures;

/**
 * Fixture handler plugin which can intercept object deserialization and provide its
 * own behavior during fixture instantiation.
 * <p/>
 * Date: Mar 25, 2009
 * <p/>
 * Time: 11:26:54 AM
 *
 * @param <SourceType> type of source object provided by FixtureSource
 * @param <ReturnType> type of object returned by this handler
 * @author Steve Reed
 */
public interface FixtureHandler<SourceType, ReturnType> {

	/**
	 * Returns the type of object created by this handler.
	 * @return the type of object created by this handler
	 */
	Class<? extends ReturnType> getType();

	/**
	 * Deserializes the given object from a fixture source object.
	 *
	 * @param desiredType type required by consumer
	 * @param rawValue raw value
	 * @param name property name
	 * @return value
	 */
	ReturnType deserialize(Class desiredType, SourceType rawValue, String name);
}
