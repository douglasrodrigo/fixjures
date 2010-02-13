package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.Suppliers;
import com.bigfatgun.fixjures.handlers.AbstractUnmarshaller;
import com.bigfatgun.fixjures.handlers.Unmarshaller;
import com.bigfatgun.fixjures.handlers.UnmarshallingContext;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.Proxies;
import com.bigfatgun.fixjures.proxy.ProxyUtils;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bigfatgun.fixjures.FixtureException.convert;

final class YamlHandlers {

	private static class ValueExtractor implements Function<Supplier<?>, Object> {
		public Object apply(@Nullable final Supplier<?> valueProvider) {
			return valueProvider.get();
		}
	}

	private YamlHandlers() {
	}

	private static abstract class ListHandler<T> extends AbstractUnmarshaller<T> {
		protected ListHandler(final Class<T> returnType) {
			super(List.class, returnType);
		}

		public final Supplier<? extends T> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
			final List jsonArray = castSourceValue(List.class, source);
			final ImmutableList<Supplier<?>> providers = objectsInArray(helper, jsonArray, typeDef);
			return convertJsonArray(providers);
		}

		private ImmutableList<Supplier<?>> objectsInArray(final UnmarshallingContext helper, final List array, final FixtureType collectionType) {
			final List<Supplier<?>> source = Lists.newLinkedList();

			for (final Object sourceValue : array) {
				final Supplier<?> unmarshalled = helper.unmarshall(sourceValue, collectionType.collectionType());
				source.add(unmarshalled);
			}

			return ImmutableList.copyOf(source);
		}

		protected abstract Supplier<? extends T> convertJsonArray(final ImmutableList<Supplier<?>> source);
	}

	public static Unmarshaller<Set> newSetHandler() {
		return new ListHandler<Set>(Set.class) {
			@Override
			protected Supplier<HashSet<Object>> convertJsonArray(final ImmutableList<Supplier<?>> source) {
				return Suppliers.of(Sets.newHashSet(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<List> newListHandler() {
		return new ListHandler<List>(List.class) {
			@Override
			protected Supplier<? extends List> convertJsonArray(final ImmutableList<Supplier<?>> source) {
				return Suppliers.of(Lists.newArrayList(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static Unmarshaller<Multiset> newMultisetHandler() {
		return new ListHandler<Multiset>(Multiset.class) {
			@Override
			protected Supplier<? extends Multiset> convertJsonArray(final ImmutableList<Supplier<?>> source) {
				return Suppliers.of(HashMultiset.create(Iterables.transform(source, new ValueExtractor())));
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
				return Suppliers.of(actualArray);
			}
		};
	}

	public static Unmarshaller<Map> newMapHandler() {
		return new AbstractUnmarshaller<Map>(Map.class, Map.class) {

			public Supplier<? extends Map> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
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
				return Suppliers.of(Maps.newHashMap(builder.build()));
			}
		};
	}

	public static Unmarshaller<?> newObjectProxyHandler() {
		return new AbstractUnmarshaller<Object>(YamlSource.YamlMap.class, Object.class) {
			public Supplier<Object> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				final ObjectProxy<?> proxy = Proxies.newProxy(typeDef.getType(), helper.getOptions());
				configureProxy(helper, proxy, castSourceValue(YamlSource.YamlMap.class, source).get());
				try {
					return Suppliers.of(proxy.create());
				} catch (Exception e) {
					throw convert(e);
				}
			}

			private <T> void configureProxy(final UnmarshallingContext helper, final ObjectProxy<T> proxy, final Map<?, ?> obj) {
				for (Object o : obj.keySet()) {
					final String key = o.toString();
					final String methodName = helper.getOptions().contains(Fixjure.Option.LITERAL_MAPPING) ? key : ProxyUtils.getterName(key);
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
								return helper.unmarshall(obj.get(key), getterTypeDef).get();
							}
						};
					} else {
						stub = helper.unmarshall(obj.get(key), getterTypeDef);
					}

					if (stub == null) {
						if (helper.getOptions().contains(Fixjure.Option.SKIP_UNMAPPABLE)) {
							continue;
						}

						throw new FixtureException(String.format("Key [%s] (with value [%s]) found in JSON but could not stub. Could be its name or value type (%s) doesn't match methods in %s", key, obj.get(key), getterTypeDef, proxy.getType()));
					} else {
						proxy.addValueStub(methodName, stub);
					}
				}
			}
		};
	}
}
