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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.bigfatgun.fixjures.handlers.FixtureHandler;
import com.google.common.collect.ImmutableList;
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

	/** An enumeration of fixture options. */
	public static enum Option {
		/** When enabled, unmappable data from the input source will be ignored. */
		SKIP_UNMAPPABLE
	}

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger("com.bigfatgun.fixjures");

	/**
	 * Creates a builder of a list of objects.
	 *
	 * @param cls object type
	 * @param <T> object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<List<T>> listOf(final Class<T> cls) {
		return new FixtureBuilder.FixtureListBuilder<T>(cls);
	}

	/**
	 * Creates a builder of a map of objects.
	 *
	 * @param keyCls map key type
	 * @param valCls map value type
	 * @param <K> map key type
	 * @param <V> map value type
	 * @return new fixture builder
	 */
	public static <K, V> FixtureBuilder<Map<K, V>> mapOf(final Class<K> keyCls, final Class<V> valCls) {
		return new FixtureBuilder.FixtureMapBuilder<K,V>(keyCls, valCls);
	}

	/**
	 * Creates a builder of a multiset of objects.
	 *
	 * @param cls object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<Multiset<T>> multisetOf(final Class<T> cls) {
		return new FixtureBuilder.FixtureMultisetBuilder<T>(cls);
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
	 * Creates a builder of a set of objects.
	 *
	 * @param cls object type
	 * @param <T> object type
	 * @return new fixture builder
	 */
	public static <T> FixtureBuilder<Set<T>> setOf(final Class<T> cls) {
		return new FixtureBuilder.FixtureSetBuilder<T>(cls);
	}

	/**
	 * Empty private utility constructor.
	 */
	private Fixjure() {
		// utility constructor is empty
	}

	/**
    * Your basic fixture builder that provides the simplest implementation of
	 * the {@code from} method, which passes the type info into a fixture source
	 * in order to create a {@link com.bigfatgun.fixjures.Fixjure.SourcedFixtureBuilder}.
	 *
	 * @param <T> fixture object type
	 * @author Steve Reed
	 */
	public static class FixtureBuilder<T> {

		/**
		 * Builds list fixtures.
		 *
		 * @param <T> collection object type
		 */
		static final class FixtureListBuilder<T> extends FixtureBuilder<List<T>> {

			/**
			 * Creates a new fixture builder.
			 * @param cls collection object type
			 */
			FixtureListBuilder(final Class<T> cls) {
				super(List.class, ImmutableList.<Class<?>>of(cls));
			}
		}

		/**
		 * Builds set fixtures.
		 *
		 * @param <T> collection object type
		 */
		static final class FixtureSetBuilder<T> extends FixtureBuilder<Set<T>> {

			/**
			 * Creates a new fixture builder.
			 * @param cls collection object type
			 */
			FixtureSetBuilder(final Class<T> cls) {
				super(Set.class, ImmutableList.<Class<?>>of(cls));
			}
		}

		/**
		 * Builds map fixtures.
		 *
		 * @param <K> collection object key type
		 * @param <V> collection object value type
		 */
		static final class FixtureMapBuilder<K,V> extends FixtureBuilder<Map<K,V>> {

			/**
			 * Creates a new fixture builder.
			 * @param keyCls collection object key type
			 * @param valCls collection object value type
			 */
			FixtureMapBuilder(final Class<K> keyCls, final Class<V> valCls) {
				super(Map.class, ImmutableList.<Class<?>>of(keyCls, valCls));
			}
		}

		/**
		 * Builds multiset fixtures.
		 *
		 * @param <T> collection object type
		 */
		static final class FixtureMultisetBuilder<T> extends FixtureBuilder<Multiset<T>> {

			/**
			 * Creates a new fixture builder.
			 * @param cls collection object type
			 */
			FixtureMultisetBuilder(final Class<T> cls) {
				super(Multiset.class, ImmutableList.<Class<?>>of(cls));
			}
		}

		/** Fixture object type. */
		private final Class<T> clazz;

		/** List of type params. */
		private final ImmutableList<Class<?>> typeParams;

		/**
		 * Instantiates a new fixture builder.
		 *
		 * @param cls fixture object type
		 */
		FixtureBuilder(final Class<T> cls) {
			this(cls, ImmutableList.<Class<?>>of());
		}

		/**
		 * Copies the given fixture builder.
		 *
		 * @param builder builder to copy
		 */
		FixtureBuilder(final FixtureBuilder<T> builder) {
			this(builder.getType(), builder.getTypeParams());
		}

		/**
		 * Private copy ctor.
		 *
		 * @param cls class
		 * @param params type params
		 */
		@SuppressWarnings({"unchecked"})
		FixtureBuilder(final Class<?> cls, final ImmutableList<Class<?>> params) {
			clazz = (Class<T>) cls;
			typeParams = params;
		}

		/**
		 * Uses the given {@code FixtureSource} to convert this builder into
		 * a {@code SourcedFixtureBuilder}.
		 *
		 * @param source fixture source, could be JSON or otherwise
		 * @return sourced fixture builder
		 */
		public final SourcedFixtureBuilder<T> from(final FixtureSource source) {
			return new SourcedFixtureBuilder<T>(this, source);
		}

		public final StreamedFixtureBuilder<T> fromStream(final FixtureStream stream) {
			return new StreamedFixtureBuilder<T>(this, stream.asSourceStream());
		}

		/**
		 * @return fixture object type
		 */
		protected final Class<T> getType() {
			return clazz;
		}

		/**
		 * @return list of type params
		 */
		protected final ImmutableList<Class<?>> getTypeParams() {
			return ImmutableList.copyOf(typeParams);
		}

		/**
		 * Adds the given classes as type params to the main type.
		 *
		 * @param classes classes
		 * @return this
		 */
		public final FixtureBuilder<T> of(final Class<?>... classes) {
			return new FixtureBuilder<T>(clazz, ImmutableList.<Class<?>>builder().addAll(typeParams).addAll(ImmutableList.of(classes)).build());
		}
	}

	public static final class StreamedFixtureBuilder<T> extends SourcedFixtureBuilder<T> {

		/**
		 * Protected constructor that stores the given builder's state.
		 *
		 * @param builder builder to copy
		 * @param source  fixture data source
		 */
		StreamedFixtureBuilder(final FixtureBuilder<T> builder, final FixtureSource source) {
			super(builder, source);
		}

		public final Iterable<? extends T> createAll() {
			return new Iterable<T>() {
				public Iterator<T> iterator() {
					return new Iterator<T>() {
						private T next;

						public boolean hasNext() {
							try {
								next = StreamedFixtureBuilder.this.getSource().createFixture(getType(), getTypeParams());
							} catch (Exception e) {
								next = null;
							}
							return next != null;
						}

						public T next() {
							return next;
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}
					};
				}
			};
		}
	}

	/**
    * A "sourced" fixture builder, meaning it has at least the necessary state to begin
	 * reading fixtures from some type of data.
	 *
	 * @param <T> fixture object type
	 * @author Steve Reed
	 */
	public static class SourcedFixtureBuilder<T> extends FixtureBuilder<T> {

		/**
		 * Fixture data source.
		 */
		private final FixtureSource fixtureSource;

		/**
		 * Protected constructor that stores the given builder's state.
		 *
		 * @param builder builder to copy
		 * @param source fixture data source
		 */
		SourcedFixtureBuilder(final FixtureBuilder<T> builder, final FixtureSource source) {
			super(builder);
			fixtureSource = source;
		}

		/**
		 * Exposes the source to subclasses.
		 *
		 * @return the source
		 */
		protected final FixtureSource getSource() {
			return fixtureSource;
		}

		/**
		 * Adds a fixture handler to this builder.
		 *
		 * @param handler handler to add
		 * @return this
		 */
		public final SourcedFixtureBuilder<T> with(final FixtureHandler handler) {
			this.fixtureSource.installRequiredTypeHandler(handler);
			return this;
		}

		/**
		 * Strongly-typed fixture creation method which creates a new fixture object. This method forwards the call
		 * to the underlying {@code FixtureSource} which have an explicit knowledge of the fixture source format, such as
		 * the {@link com.bigfatgun.fixjures.json.JSONSource}.
		 *
		 * @return new fixture object
		 */
		public final T create() {
			try {
				return this.fixtureSource.createFixture(getType(), getTypeParams());
			} finally {
				try {
					fixtureSource.close();
				} catch (IOException e) {
					LOGGER.warning(String.format("Source close error: %s", e.getMessage()));
				}
			}
		}

		/**
		 * Adds options.
		 * @param opts options
		 * @return this
		 */
		public final SourcedFixtureBuilder<T> withOptions(final Option... opts) {
			for (final Option opt : opts) {
				this.fixtureSource.addOption(opt);
			}
			return this;
		}

		/**
		 * Adds options.
		 * @param opts options
		 * @return this
		 */
		public final SourcedFixtureBuilder<T> withOptions(final Set<Option> opts) {
			for (final Option opt : opts) {
				this.fixtureSource.addOption(opt);
			}
			return this;
		}
	}
}
