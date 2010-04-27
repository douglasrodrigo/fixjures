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

package com.bigfatgun.fixjures.handlers;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.ObjectProxyData;
import com.bigfatgun.fixjures.proxy.Proxies;
import com.bigfatgun.fixjures.proxy.ProxyUtils;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class Unmarshallers {

	private static class ValueExtractor implements Function<Supplier<?>, Object> {
		public Object apply(final Supplier<?> valueProvider) {
			return valueProvider.get();
		}
	}

	private static abstract class ListHandler<T> extends AbstractUnmarshaller<T> {
		protected ListHandler(final Class<T> returnType) {
			super(List.class, returnType);
		}

		public final Supplier<? extends T> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
			final List list = castSourceValue(List.class, source);
			final ImmutableList<Supplier<?>> providers = objectsInArray(helper, list, typeDef);
			return convertList(providers);
		}

		private ImmutableList<Supplier<?>> objectsInArray(final UnmarshallingContext helper, final List array, final FixtureType collectionType) {
			final List<Supplier<?>> source = Lists.newLinkedList();

			for (final Object sourceValue : array) {
				final Supplier<?> unmarshalled = helper.unmarshall(sourceValue, collectionType.collectionType());
				source.add(unmarshalled);
			}

			return ImmutableList.copyOf(source);
		}

		protected abstract Supplier<? extends T> convertList(final ImmutableList<Supplier<?>> source);
	}

    public static Unmarshaller<Boolean> boolHandler() {
        return new PrimitiveUnmarshaller<Boolean>() {
            @Override
            public Class<? extends Boolean> getPrimitiveType() {
                return Boolean.TYPE;
            }

            @Override
            public boolean canUnmarshallObjectToType(Object sourceObject, FixtureType typeDef) {
                return sourceObject instanceof Boolean 
                        && (typeDef.isA(getReturnType()) || typeDef.isA(getPrimitiveType()));
            }

            @Override
            public Class<Boolean> getReturnType() {
                return Boolean.class;
            }

            @Override
            public Supplier<? extends Boolean> unmarshall(UnmarshallingContext helper, Object source, FixtureType typeDef) {
                return Suppliers.ofInstance((Boolean) source);
            }
        };
    }

	public static Unmarshaller<Byte> byteHandler() {
		return new NumberUnmarshaller<Byte>(Byte.class, Byte.TYPE) {
			@Override
			protected Byte narrowNumericValue(final Number number) {
				return number.byteValue();
			}
		};
	}

	public static Unmarshaller<Short> shortHandler() {
		return new NumberUnmarshaller<Short>(Short.class, Short.TYPE) {
			@Override
			protected Short narrowNumericValue(final Number number) {
				return number.shortValue();
			}
		};
	}

	public static Unmarshaller<Integer> integerHandler() {
		return new NumberUnmarshaller<Integer>(Integer.class, Integer.TYPE) {
			@Override
			protected Integer narrowNumericValue(final Number number) {
				return number.intValue();
			}
		};
	}

	public static Unmarshaller<Long> longHandler() {
		return new NumberUnmarshaller<Long>(Long.class, Long.TYPE) {
			@Override
			protected Long narrowNumericValue(final Number number) {
				return number.longValue();
			}
		};
	}

	public static Unmarshaller<Float> floatHandler() {
		return new NumberUnmarshaller<Float>(Float.class, Float.TYPE) {
			@Override
			protected Float narrowNumericValue(final Number number) {
				return number.floatValue();
			}
		};
	}

	public static Unmarshaller<Double> doubleHandler() {
		return new NumberUnmarshaller<Double>(Double.class, Double.TYPE) {
			@Override
			protected Double narrowNumericValue(final Number number) {
				return number.doubleValue();
			}
		};
	}

	public static Unmarshaller<StringBuilder> stringBuilderHandler() {
		return new StringBuilderUnmarshaller();
	}

	public static Unmarshaller<BigInteger> bigIntegerHandler() {
		return new BigIntegerUnmarshaller();
	}

	public static Unmarshaller<BigDecimal> bigDecimalHandler() {
		return new BigDecimalUnmarshaller();
	}

	public static Unmarshaller<Date> javaDateHandler() {
		return new DateUnmarshaller();
	}

	public static Unmarshaller<Set> newSetHandler() {
		return new ListHandler<Set>(Set.class) {
			@Override
			protected Supplier<HashSet<Object>> convertList(final ImmutableList<Supplier<?>> source) {
				return Suppliers.ofInstance(Sets.newHashSet(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<List> newListHandler() {
		return new ListHandler<List>(List.class) {
			@Override
			protected Supplier<? extends List> convertList(final ImmutableList<Supplier<?>> source) {
				return Suppliers.ofInstance(Lists.newArrayList(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<Collection> newCollectionHandler() {
		return new ListHandler<Collection>(Collection.class) {
			@Override
			protected Supplier<? extends Collection> convertList(final ImmutableList<Supplier<?>> source) {
				return Suppliers.ofInstance(Lists.newArrayList(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<Multiset> newMultisetHandler() {
		return new ListHandler<Multiset>(Multiset.class) {
			@Override
			protected Supplier<? extends Multiset> convertList(final ImmutableList<Supplier<?>> source) {
				return Suppliers.ofInstance(HashMultiset.create(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<Object> newArrayHandler() {
		return new Unmarshaller<Object>() {
			public boolean canUnmarshallObjectToType(final Object obj, final FixtureType desiredType) {
				return desiredType.getType().isArray() && obj instanceof List;
			}

			public Class<Object> getReturnType() {
				return Object.class;
			}

			public Supplier<Object> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				final List array = (List) source;
				final FixtureType definition = typeDef.collectionType();
				final Class<?> collectionType = definition.getType();
				final Object actualArray = Array.newInstance(collectionType, array.size());
				for (int i = 0; i < array.size(); i++) {
					final Supplier<?> arraySupplier = helper.unmarshall(array.get(i), definition);
					final Object arrayValue = arraySupplier.get();
					Array.set(actualArray, i, arrayValue);
				}
				return Suppliers.ofInstance(actualArray);
			}
		};
	}

	public static Unmarshaller<Map> newMapHandler() {
		return new AbstractUnmarshaller<Map>(Map.class, Map.class) {

			public Supplier<ImmutableMap<Object, Object>> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				final Map sourceObject = castSourceValue(Map.class, source);
				final FixtureType keyType = typeDef.keyType();
				final FixtureType valueType = typeDef.valueType();
				ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
				for (final Object lookupKey : sourceObject.keySet()) {
					final Supplier<?> keyProvider = help(helper, lookupKey, keyType);
					final Supplier<?> valueProvider = help(helper, sourceObject.get(String.valueOf(lookupKey)), valueType);
					final Object key = keyProvider.get();
					final Object keyValue = valueProvider.get();
					if (key != null && keyValue != null) {
						builder = builder.put(key, keyValue);
					}
				}
				return Suppliers.ofInstance(builder.build());
			}
		};
	}

	public static Unmarshaller<?> newObjectProxyHandler() {
		return new AbstractUnmarshaller<Object>(ObjectProxyData.class, Object.class) {
			public Supplier<?> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				final ObjectProxy<?> proxy = Proxies.newProxy(typeDef.getType(), helper.getOptions());
				configureProxy(helper, proxy, castSourceValue(ObjectProxyData.class, source).get());
				if (helper.getOptions().contains(Fixjure.Option.LAZY_REFERENCE_EVALUATION)) {
					return proxy;
				} else {
					try {
						return proxy;
					} catch (Exception e) {
						throw convert(e);
					}
				}
			}

			private <T> void configureProxy(final UnmarshallingContext helper, final ObjectProxy<T> proxy, final Map<?, ?> obj) {
				for (Object o : obj.keySet()) {
					final String key = o.toString();
					final String methodName = helper.getOptions().contains(Fixjure.Option.LITERAL_MAPPING) ? key : ProxyUtils.getterName(proxy.getType(), key);
					final FixtureType getterTypeDef = proxy.suggestType(methodName);
					if (getterTypeDef == null) {
						if (helper.getOptions().contains(Fixjure.Option.SKIP_UNMAPPABLE)) {
							continue;
						} else {
                            throw new FixtureException("Could not find type of method: " + methodName);
                        }
					}

					final Supplier<?> stub;
					if (helper.getOptions().contains(Fixjure.Option.LAZY_REFERENCE_EVALUATION)) {
						stub = new Supplier<Object>() {
							public Object get() {
								Supplier<?> supplier = helper.unmarshall(obj.get(key), getterTypeDef);
								if (supplier == null) {
									throw new FixtureException(String.format("Could not find unmarshaller for %s (%s)", obj.get(key), getterTypeDef));
								}
								return supplier.get();
							}
						};
					} else {
						stub = helper.unmarshall(obj.get(key), getterTypeDef);
					}

					if (stub == null) {
						if (helper.getOptions().contains(Fixjure.Option.SKIP_UNMAPPABLE)) {
							continue;
						}

						throw new FixtureException(String.format("Key [%s] (with value [%s] (%s)) found in source but " +
                                "could not stub. Could be its name or value type (%s) doesn't match methods in %s",
                                key,
                                obj.get(key),
                                (obj.get(key) == null) ? "??" : obj.get(key).getClass().getSimpleName(),
                                getterTypeDef,
                                proxy.getType()));
					} else {
                        proxy.addValueStub(methodName, stub);
                    }
				}
			}
		};
	}
}
