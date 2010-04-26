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

package com.bigfatgun.fixjures;


/** Interface marks an entity that is able to find and create/retrieve an object stub by type and id. */
public interface IdentityResolver {

	/**
	 * @param requiredType object type
	 * @param rawIdentityValue object id in raw form
	 * @return true if this provider can resolve the given identity value
	 */
	boolean canHandleIdentity(Class<?> requiredType, Object rawIdentityValue);

	/**
	 * Converts the raw identity value into a string.
	 *
	 * @param rawIdentityValue raw identity value
	 * @return identty as string
	 */
	String coerceIdentity(Object rawIdentityValue);

	/**
	 * Resolves an object by type and id.
	 *
	 * @param requiredType object type
	 * @param id object id
	 * @return object; if not found, null is returned
	 */
	<T> T resolve(Class<T> requiredType, String id);
}
