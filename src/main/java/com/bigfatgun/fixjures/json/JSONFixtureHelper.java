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
package com.bigfatgun.fixjures.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.bigfatgun.fixjures.Fixjure;

/**
 * This utility will scan an object for methods with the {@link JSONFixture} annotation and invoke said methods
 * with appropriate fixture objects.
 *
 * @author Steve Reed
 */
public final class JSONFixtureHelper {

	/**
	 * Scans the object for public void single-argument methods with the {@link JSONFixture} annotation. The annotation
	 * provides enough information to create a JSON-sourced fixture object, which is then passed to the annotated
	 * method.
	 * <p>
	 * <code>@JSONFixture(value = "{ firstName : \"Steve\", lastName : \"Reed\" }")
	 * public void setAuthor(final Person person) {
	 *    // impl
	 * }
	 * </code>
	 *
	 * @param obj object to scan
	 * @throws FileNotFoundException if the JSONFixture is sourced from a file which is not found
	 * @throws InvocationTargetException if the method can't be invoked via reflection
	 * @throws IllegalAccessException if the method can't be accessed
	 */
	public static void scan(final Object obj) throws FileNotFoundException, InvocationTargetException, IllegalAccessException {
		final Class cls = obj.getClass();
		for (Method m : cls.getMethods()) {
         if (m.isAnnotationPresent(JSONFixture.class)) {
				// found one
				// want a signature with one argument, matching annotation type
				// and no return code?
				final Class[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 1 && m.getReturnType() == Void.TYPE) {
					// we're cool
					final JSONFixture fixture = m.getAnnotation(JSONFixture.class);
					final JSONSource fixtureSource;
					if (fixture.type() == JSONSource.SourceType.FILE) {
						//noinspection UnusedAssignment
						fixtureSource = new JSONSource(new File(fixture.value()));
					} else {
						//noinspection UnusedAssignment
						fixtureSource = new JSONSource(fixture.value());
					}
					//noinspection unchecked,RedundantArrayCreation
					m.invoke(obj, new Object[] { Fixjure.of(paramTypes[0]).from(fixtureSource).create() });
				}
			}
      }
	}

	/**
	 * Private utility constructor.
	 */
	private JSONFixtureHelper() {
		// private constructor
	}
}
