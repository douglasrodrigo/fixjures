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
package com.bigfatgun.fixjures;

/**
 * A runtime exception used during fixture object creation to wrap any sort
 * of underlying failure.
 */
public class FixtureException extends RuntimeException {

	/**
	 * Ensures that the given exception is returned as a FixtureException (if it passes an
	 * instanceof check) or is wrapped by a new one.
	 *
	 * @param cause causing exception
	 * @return fixture exception
	 */
	public static FixtureException convert(final Throwable cause) {
		if (cause instanceof FixtureException) {
			return (FixtureException) cause;
		} else {
			return new FixtureException(cause);
		}
	}

	/**
	 * Creates a new exception.
	 * @param message message
	 */
	public FixtureException(final String message) {
		super(message);
	}

	/**
	 * Creates a new exception.
	 * @param throwable cause
	 */
	public FixtureException(final Throwable throwable) {
		super(throwable);
	}
}
