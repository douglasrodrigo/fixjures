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

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Multiset;

/**
 * Main fixjures entry point that provides builder-like semantics for setting up
 * and creating fixtures.
 * <p>
 * Example: {@code MyClass my = Fixjure.of(MyClass.class).from(source).create();}
 *
 * @author Steve Reed
 */
public class Fixjure {

	/** Logger. */
	public static final Logger LOGGER = Logger.getLogger("com.bigfatgun.fixjures");

	/**
	 * Empty private utility constructor.
	 */
	private Fixjure() {
		// utility constructor is empty
	}

	/**
	 * Creates a new un-sourced fixture builder of the given class.
	 *
	 * @param cls fixture class
	 * @param <T> fixture type
	 * @return new un-sourced fixture builder
	 */
	public static <T> FixtureBuilder<T> of(final Class<T> cls) {
		return new FixtureBuilder<T>(cls);
	}

	/**
	 * Creates a builder of a list of objects.
	 *
	 * @param cls object type
	 * @param <T> object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<List<T>> listOf(final Class<T> cls) {
		return new FixtureBuilder<List<T>>(List.class).of(cls);
	}

	/**
	 * Creates a builder of a set of objects.
	 *
	 * @param cls object type
	 * @param <T> object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<Set<T>> setOf(final Class<T> cls) {
		return new FixtureBuilder<Set<T>>(Set.class).of(cls);
	}

	/**
	 * Creates a builder of a multiset of objects.
	 *
	 * @param cls object type
	 * @param <T> object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<Multiset<T>> multisetOf(final Class<T> cls) {
		return new FixtureBuilder<Multiset<T>>(Multiset.class).of(cls);
	}
}
