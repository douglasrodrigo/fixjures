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

public final class ProxyUtils {
	private ProxyUtils() {}

	/**
	 * Converts a property name into a getter name. For example, "firstName" will be transformed into "getFirstName".
	 */
	public static String getterName(final Class<?> cls, final String propertyName) {
        String normalGetter = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        String booleanGetter = "is" + normalGetter.substring(3);
        for (String getter : new String[] { normalGetter, booleanGetter }) {
            try {
                return cls.getMethod(getter).getName();
            } catch (NoSuchMethodException e) {
                // ignore
            }
        }
        return null;
    }

	/**
	 * Converts getter name to setter name by replacing the first "g" with an "s".
	 *
	 * @param getterName getter name
	 * @return setter name
	 */
	public static String convertNameToSetter(final String getterName) {
        for (String prefix : new String[] { "get", "is" }) {
            if (getterName.startsWith(prefix)) {
                return "set" + getterName.substring(prefix.length());
            }
        }
        return null;
    }
}
