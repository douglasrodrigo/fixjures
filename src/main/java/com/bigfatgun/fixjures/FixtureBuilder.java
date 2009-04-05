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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

/**
 * Your basic fixture builder that provides the simplest implementation of
 * the {@code from} method, which passes the type info into a fixture source
 * in order to create a {@link SourcedFixtureBuilder}.
 *
 * @param <T> fixture object type
 * @author Steve Reed
 */
public class FixtureBuilder<T> {

	/**
	 * Builds list fixtures.
	 *
	 * @param <T> collection object type
	 */
	/* package */ static final class FixtureListBuilder<T> extends FixtureBuilder<List<T>> {

		/**
		 * Creates a new fixture builder.
		 * @param cls collection object type
		 */
		FixtureListBuilder(final Class<T> cls) {
			super(List.class);
			of(cls);
		}
	}

	/**
	 * Builds set fixtures.
	 *
	 * @param <T> collection object type
	 */
	/* package */ static final class FixtureSetBuilder<T> extends FixtureBuilder<Set<T>> {

		/**
		 * Creates a new fixture builder.
		 * @param cls collection object type
		 */
		FixtureSetBuilder(final Class<T> cls) {
			super(Set.class);
			of(cls);
		}
	}

	/**
	 * Builds map fixtures.
	 *
	 * @param <K> collection object key type
	 * @param <V> collection object value type
	 */
	/* package */ static final class FixtureMapBuilder<K,V> extends FixtureBuilder<Map<K,V>> {

		/**
		 * Creates a new fixture builder.
		 * @param keyCls collection object key type
		 * @param valCls collection object value type
		 */
		FixtureMapBuilder(final Class<K> keyCls, final Class<V> valCls) {
			super(Map.class);
			of(keyCls, valCls);
		}
	}

	/**
	 * Builds multiset fixtures.
	 *
	 * @param <T> collection object type
	 */
	/* package */ static final class FixtureMultisetBuilder<T> extends FixtureBuilder<Multiset<T>> {

		/**
		 * Creates a new fixture builder.
		 * @param cls collection object type
		 */
		FixtureMultisetBuilder(final Class<T> cls) {
			super(Multiset.class);
			of(cls);
		}
	}

	/** Fixture object type. */
	private final Class<T> clazz;

	/** List of type params. */
	private final List<Class<?>> typeParams;

	/**
	 * Instantiates a new fixture builder.
	 *
	 * @param cls fixture object type
	 */
	/* package */
	@SuppressWarnings({"unchecked"})
	FixtureBuilder(final Class cls) {
		clazz = (Class<T>) cls;
		typeParams = Lists.newLinkedList();
	}

	/**
	 * Copies the given fixture builder.
	 *
	 * @param builder builder to copy
	 */
	/* package */ FixtureBuilder(final FixtureBuilder<T> builder) {
		clazz = builder.getType();
		typeParams = builder.getTypeParams();
	}

	/**
	 * Uses the given {@code FixtureSource} to convert this builder into
	 * a {@code SourcedFixtureBuilder}.
	 *
	 * @param source fixture source, could be JSON or otherwise
	 * @return sourced fixture builder
	 */
	public final SourcedFixtureBuilder<T, ? extends FixtureSource> from(final FixtureSource source) {
		return source.build(this);
	}

	/**
	 * @return fixture object type
	 */
	public final Class<T> getType() {
		return clazz;
	}

	/**
	 * @return list of type params
	 */
	public final ImmutableList<Class<?>> getTypeParams() {
		return ImmutableList.copyOf(typeParams);
	}

	/**
	 * Adds the given classes as type params to the main type.
	 *
	 * @param classes classes
	 * @return this
	 */
	public final FixtureBuilder<T> of(final Class<?>... classes) {
		typeParams.addAll(ImmutableList.of(classes));
		return this;
	}
}
