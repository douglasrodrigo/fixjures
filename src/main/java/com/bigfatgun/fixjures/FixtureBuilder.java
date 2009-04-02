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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Your basic fixture builder that provides the simplest implementation of
 * the {@code from} method, which passes the type info into a fixture source
 * in order to create a {@link SourcedFixtureBuilder}.
 *
 * @param <T> fixture object type
 * @author Steve Reed
 */
public class FixtureBuilder<T> {

	/** Fixture object type. */
	private final Class<? super T> clazz;

	/** List of type params. */
	private final List<Class<?>> typeParams;

	/**
	 * Instantiates a new fixture builder.
	 *
	 * @param cls fixture object type
	 */
	/* package */ FixtureBuilder(final Class<? super T> cls) {
		clazz = cls;
		typeParams = Lists.newLinkedList();
	}

	/**
	 * @return list of type params
	 */
	public List<Class<?>> getTypeParams() {
		return typeParams;
	}

	/**
	 * Uses the given {@code FixtureSource} to convert this builder into
	 * a {@code SourcedFixtureBuilder}.
	 *
	 * @param source fixture source, could be JSON or otherwise
	 * @return sourced fixture builder
	 */
	public SourcedFixtureBuilder<T, ? extends FixtureSource> from(final FixtureSource source) {
		return source.build(this);
	}

	/**
	 * @return fixture object type
	 */
	@SuppressWarnings({"unchecked"})
	public final Class<T> getType() {
		return (Class<T>) clazz;
	}

	/**
	 * Adds the given classes as type params to the main type.
	 *
	 * @param classes classes
	 * @return this
	 */
	public FixtureBuilder<T> of(final Class<?>... classes) {
		Iterables.addAll(typeParams, Arrays.asList(classes));
		return this;
	}
}
