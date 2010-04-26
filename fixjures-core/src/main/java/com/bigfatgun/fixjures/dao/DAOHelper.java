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

package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.IdentifierProvider;
import com.bigfatgun.fixjures.IdentityResolver;
import com.bigfatgun.fixjures.SourceFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Collections2.filter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import com.google.common.collect.Ordering;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DAOHelper<T> {

	private static final class EmptyByteChannel implements ReadableByteChannel {
		@Override
		public int read(ByteBuffer dst) throws IOException {
			return -1;
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public void close() throws IOException {
			// ignore
		}
	}

	private static class ImmutableIdentifierProvider implements IdentifierProvider {

		private final ImmutableList<String> ids;

		public ImmutableIdentifierProvider(Iterable<String> identifiers) {
			ids = ImmutableList.copyOf(identifiers);
		}

		@Override
		public Iterable<String> existingObjectIdentifiers() {
			return ids;
		}
	}
	
	private static final class EmptyIdentifierProvider extends ImmutableIdentifierProvider {

		public EmptyIdentifierProvider() {
			super(ImmutableList.<String>of());
		}
	}

    private static class ListBackedDAOHelper<T> extends DAOHelper<T> {

        private static class ListBasedIdentityResolver<T> implements IdentityResolver {

            private final Class<T> cls;
            private final ImmutableList<T> startingList;
            private final Map<String, T> allObjects;
            private final Set<String> validIds;
            private final Function<? super T, String> idFunction;

            public ListBasedIdentityResolver(Class<T> cls, FixtureSource source, Function<? super T, String> idFunction) {
                this.cls = cls;
                this.startingList = ImmutableList.copyOf(Fixjure.listOf(cls).from(source).withOptions(
                        Fixjure.Option.LAZY_REFERENCE_EVALUATION, 
                        Fixjure.Option.NULL_ON_UNMAPPED,
                        Fixjure.Option.SKIP_UNMAPPABLE
                ).resolveIdsWith(this).create());
                this.idFunction = idFunction;
                this.allObjects = newHashMap();
                for (T t : startingList) {
                    this.allObjects.put(idFunction.apply(t), t);
                }
                this.validIds = newHashSet(allObjects.keySet());
            }

            public List<T> getList() {
                return Lists.newLinkedList(filter(allObjects.values(), compose(in(validIds), idFunction)));
            }

            public T add(T object, String identifier) {
                allObjects.put(identifier, object);
                validIds.add(identifier);
                return object;
            }

            public T remove(String identifier) {
                validIds.remove(identifier);
                return allObjects.remove(identifier);
            }

            @Override
            public boolean canHandleIdentity(Class<?> requiredType, Object rawIdentityValue) {
                return requiredType == cls;
            }

            @Override
            public String coerceIdentity(Object rawIdentityValue) {
                return String.valueOf(rawIdentityValue);
            }

            @Override
            public <T> T resolve(Class<T> requiredType, String id) {
                if (validIds.contains(id)) {
                    return requiredType.cast(allObjects.get(id));
                } else {
                    return null;
                }
            }
        }

        private final ListBasedIdentityResolver<T> list;

        @SuppressWarnings({"unchecked"})
        private ListBackedDAOHelper(final Class<T> cls, FixtureSource source, Function<? super T, String> idFunction) {
            super(cls, new ListBasedIdentityResolver<T>(cls, source, idFunction));

            this.list = (ListBasedIdentityResolver<T>) getIdResolver();
        }

        @Override
        public T add(T object, String identifier) {
            return list.add(object, identifier);
        }

        @Override
        public T remove(String identifier) {
            return list.remove(identifier);
        }

        @Override
        public Iterable<T> findAll() {
            return list.getList();
        }
    }

    private static class FactoryBackedDAOHelper<T> extends DAOHelper<T> {

        private final Set<String> identifiers;
        private final Function<String, T> loadByIdFunction;
        private final FixtureFactory factory;

        private FactoryBackedDAOHelper(final Class<T> cls, final SourceFactory source, final IdentifierProvider idProvider) {
            super(cls, FixtureFactory.newFactory(source)
                    .enableOption(Fixjure.Option.LAZY_REFERENCE_EVALUATION)
                    .enableOption(Fixjure.Option.NULL_ON_UNMAPPED)
                    .enableOption(Fixjure.Option.SKIP_UNMAPPABLE));
            this.identifiers = newHashSet(idProvider.existingObjectIdentifiers());
            this.loadByIdFunction = new Function<String, T>() {
                @Override
                public T apply(String s) {
                    return FactoryBackedDAOHelper.this.findById(s);
                }
            };
            this.factory = (FixtureFactory) getIdResolver();
        }

        public T findById(final String id) {
            return (identifiers.contains(id)) ? super.findById(id) : null;
        }

        public T add(final T object, final String identifier) {
            addIdentifier(identifier);
            return factory.cache(getType(), object, identifier);
        }

        public T remove(final String identifier) {
            removeIdentifier(identifier);
            return factory.uncache(getType(), identifier);
        }

        private boolean addIdentifier(final String id) {
            return identifiers.add(id);
        }

        private boolean removeIdentifier(final String id) {
            return identifiers.remove(id);
        }

        private void setIdentifiers(Collection<String> newIdentifiers) {
            identifiers.clear();
            identifiers.addAll(newIdentifiers);
        }

        public Iterable<T> findAll() {
            return Iterables.transform(identifiers, loadByIdFunction);
        }
    }

	public static <T> DAOHelper<T> forClass(final Class<T> cls) {
		return forClass(cls, new SourceFactory() {
			@Override
			public FixtureSource newInstance(Class<?> fixtureType, String fixtureId) {
				return new FixtureSource(new EmptyByteChannel()) {
					@Override
					protected Object createFixture(FixtureType type) {
						return null;
					}
				};
			}
		}, new EmptyIdentifierProvider());
	}

    public static <T> DAOHelper<T> forClassFromSingleSource(final Class<T> cls, FixtureSource source, Function<? super T, String> idFunction) {
        return new ListBackedDAOHelper<T>(cls, source, idFunction);
    }

	public static <T> DAOHelper<T> forClass(final Class<T> cls, final SourceFactory factory, final IdentifierProvider idProvider) {
		return new FactoryBackedDAOHelper<T>(cls, factory, idProvider);
	}

    private final Class<T> cls;
    private final IdentityResolver resolver;

    protected DAOHelper(Class<T> cls, IdentityResolver resolver) {
        this.cls = cls;
        this.resolver = resolver;
    }

    protected final IdentityResolver getIdResolver() {
        return resolver;
    }

    public final Class<T> getType() {
        return cls;
    }

    public T findById(final String id) {
        return resolver.resolve(getType(), id);
    }

    public abstract T add(final T object, final String identifier);

    public abstract T remove(final String identifier);

    public abstract Iterable<T> findAll();

    public final Iterable<T> findAllWhere(final Predicate<? super T> condition) {
        return Iterables.filter(findAll(), condition);
    }

    public final int findIndexOfObjectInOrder(final T object, final Ordering<? super T> ordering) {
        return ordering.binarySearch(findAllOrdered(ordering), object);
    }

    public final List<T> findAllOrdered(final Ordering<? super T> ordering) {
        return ordering.sortedCopy(findAll());
    }

    public final List<T> findAllOrderedWhere(final Ordering<? super T> ordering, final Predicate<? super T> condition) {
        return findAllOrderedWhere(ordering, condition, true);
    }

    public final List<T> findAllOrderedWhere(final Ordering<? super T> ordering, final Predicate<? super T> condition, boolean filterBeforeSort) {
        if (filterBeforeSort) {
            return ordering.sortedCopy(findAllWhere(condition));
        } else {
            return newArrayList(Iterables.filter(findAllOrdered(ordering), condition));
        }
    }
}
