/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
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

/** Handles conversion of source data to destination data. */
public interface Unmarshaller<T> {

	boolean canUnmarshallObjectToType(Object sourceObject, FixtureType typeDef);

	Class<T> getReturnType();

	Supplier<? extends T> unmarshall(UnmarshallingContext helper, Object source, FixtureType typeDef);
}
