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

package com.bigfatgun.fixjures.proxy;

import com.bigfatgun.fixjures.FixtureType;
import com.google.common.base.Supplier;

/**
 * This interface is implemented by any class that can produce a proxy object of some sort based on a pre-configured map
 * of property accessor name to value.
 *
 * @author Steve Reed
 */
public interface ObjectProxy<T> extends Supplier<T> {

	/**
	 * Returns the proxy object type
	 *
	 * @return the proxy object type
	 */
	Class<T> getType();

	/**
	 * Adds a value stub for the given method name.
	 *
	 * @param methodName method name
	 * @param stub value stub
	 */
	void addValueStub(String methodName, Supplier<?> stub);

	FixtureType suggestType(String key);
}
