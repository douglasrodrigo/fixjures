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
package com.bigfatgun.fixjures.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic fixture object injection. This annotation can be used on fields or simple
 * "java bean" setter methods.
 *
 * @author Steve Reed
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
public @interface JSONFixture {

	/**
	 * Fixture value, either a json string literal if location is LITERAL, or a filename if it is FILE.
	 * @return fixture value
	 */
	String value() default "{}";

	/**
	 * Fixture source type, either a string literal or a file.
	 * @return fixture source type
	 */
	JSONSource.SourceType location() default JSONSource.SourceType.LITERAL;

	/**
	 * Fixture object type.
	 * @return fixture object type
	 */
	Class type();
}
