package com.bigfatgun.fixjures.json;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;
import com.bigfatgun.fixjures.handlers.AbstractFixtureHandler;
import com.bigfatgun.fixjures.handlers.FixtureHandler;
import com.bigfatgun.fixjures.handlers.HandlerHelper;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.Proxies;
import com.bigfatgun.fixjures.proxy.ProxyUtils;
import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class JsonHandlers {

	private static class ValueExtractor implements Function<ValueProvider<?>, Object> {
		@Override
		public Object apply(@Nullable final ValueProvider<?> valueProvider) {
			return valueProvider.get();
		}
	}

	private JsonHandlers() {
	}

	private static abstract class JsonArrayHandler<T> extends AbstractFixtureHandler<T> {
		protected JsonArrayHandler(final Class<T> returnType) {
			super(JSONArray.class, returnType);
		}

		@Override
		public final ValueProvider<? extends T> apply(final HandlerHelper helper, final FixtureType typeDef, final Object source) {
			try {
				final JSONArray jsonArray = castSourceValue(JSONArray.class, source);
				final ImmutableList<ValueProvider<?>> providers = objectsInArray(helper, jsonArray, typeDef);
				return convertJsonArray(providers);
			} catch (JSONException e) {
				throw FixtureException.convert(e);
			}
		}

		private ImmutableList<ValueProvider<?>> objectsInArray(final HandlerHelper helper, final JSONArray array, final FixtureType collectionType) throws JSONException {
			final List<ValueProvider<?>> source = Lists.newLinkedList();

			for (int i = 0; i < array.length(); i++) {
				final Object sourceValue = array.get(i);
				final FixtureHandler<?> handler = helper.findHandler(sourceValue, collectionType.collectionType());
				source.add(handler.apply(helper, collectionType.collectionType(), sourceValue));
			}

			return ImmutableList.copyOf(source);
		}

		protected abstract ValueProvider<? extends T> convertJsonArray(final ImmutableList<ValueProvider<?>> source);
	}

	public static FixtureHandler<Set> newSetHandler() {
		return new JsonArrayHandler<Set>(Set.class) {
			@Override
			protected ValueProvider<HashSet<Object>> convertJsonArray(final ImmutableList<ValueProvider<?>> source) {
				return ValueProviders.of(Sets.newHashSet(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static FixtureHandler<List> newListHandler() {
		return new JsonArrayHandler<List>(List.class) {
			@Override
			protected ValueProvider<? extends List> convertJsonArray(final ImmutableList<ValueProvider<?>> source) {
				return ValueProviders.of(Lists.newArrayList(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static FixtureHandler<Multiset> newMultisetHandler() {
		return new JsonArrayHandler<Multiset>(Multiset.class) {
			@Override
			protected ValueProvider<? extends Multiset> convertJsonArray(final ImmutableList<ValueProvider<?>> source) {
				return ValueProviders.of(HashMultiset.create(Iterables.transform(source, new ValueExtractor())));
			}
		};
	}

	public static FixtureHandler<Object> newArrayHandler() {
		return new FixtureHandler<Object>() {
			@Override
			public boolean canDeserialize(final Object obj, final Class<?> desiredType) {
				return desiredType.isArray() && obj instanceof JSONArray;
			}

			@Override
			public Class<Object> getReturnType() {
				return Object.class;
			}

			@Override
			public ValueProvider<Object> apply(final HandlerHelper helper, final FixtureType typeDef, final Object source) {
				try {
					final JSONArray array = (JSONArray) source;
					final FixtureType definition = typeDef.collectionType();
					final Class<?> collectionType = definition.getType();
					final Object actualArray = Array.newInstance(collectionType, array.length());
					for (int i = 0; i < array.length(); i++) {
						final ValueProvider<?> arrayValueProvider = helper.findHandler(array.get(i), definition).apply(helper, definition, array.get(i));
						final Object arrayValue = arrayValueProvider.get();
						Array.set(actualArray, i, arrayValue);
					}
					return ValueProviders.of(actualArray);
				} catch (JSONException e) {
					throw FixtureException.convert(e);
				}
			}
		};
	}

	public static FixtureHandler<Map> newMapHandler() {
		return new AbstractFixtureHandler<Map>(JSONObject.class, Map.class) {

			@Override
			public ValueProvider<? extends Map> apply(final HandlerHelper helper, final FixtureType typeDef, final Object source) {
				try {
					final JSONObject jsonObject = castSourceValue(JSONObject.class, source);
					final FixtureType keyType = typeDef.keyType();
					final FixtureType valueType = typeDef.valueType();
					ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
					for (final Iterator i = jsonObject.keys(); i.hasNext();) {
						final Object lookupKey = i.next();
						final ValueProvider<?> keyProvider = help(helper, lookupKey, keyType);
						final ValueProvider<?> valueProvider = help(helper, jsonObject.get(String.valueOf(lookupKey)), valueType);
						final Object key = keyProvider.get();
						final Object keyValue = valueProvider.get();
						if (key != null && keyValue != null) {
							builder = builder.put(key, keyValue);
						}
					}
					return ValueProviders.of(Maps.newHashMap(builder.build()));
				} catch (JSONException e) {
					throw FixtureException.convert(e);
				}
			}
		};
	}

	public static FixtureHandler<?> newObjectProxyHandler() {
		return new AbstractFixtureHandler<Object>(JSONObject.class, Object.class) {
			@Override
			public ValueProvider<Object> apply(final HandlerHelper helper, final FixtureType typeDef, final Object source) {
				final ObjectProxy<?> proxy = Proxies.newProxy(typeDef.getType(), helper.getOptions());
				configureProxy(helper, proxy, castSourceValue(JSONObject.class, source));
				try {
					return ValueProviders.of(proxy.create());
				} catch (Exception e) {
					throw convert(e);
				}
			}

			private <T> void configureProxy(final HandlerHelper helper, final ObjectProxy<T> proxy, final JSONObject obj) {
				final Iterator objIterator = obj.keys();
				while (objIterator.hasNext()) {
					final String key = objIterator.next().toString();
					final FixtureType getterTypeDef = proxy.suggestType(ProxyUtils.getterName(key));
					if (getterTypeDef == null) {
						continue;
					}
					try {
						final ValueProvider<?> stub;
						if (helper.getOptions().contains(Fixjure.Option.LAZY_REFERENCE_EVALUATION)) {
							stub = new ValueProvider<Object>() {
								@Override
								public Object get() {
									try {
										return helper.findHandler(obj.get(key), getterTypeDef).apply(helper, getterTypeDef, obj.get(key)).get();
									} catch (JSONException e) {
										throw convert(e);
									}
								}
							};
						} else {
							stub = helper.findHandler(obj.get(key), getterTypeDef).apply(helper, getterTypeDef, obj.get(key));
						}

						if (stub == null) {
							if (helper.getOptions().contains(Fixjure.Option.SKIP_UNMAPPABLE)) {
								continue;
							}

							throw new FixtureException(String.format("Key [%s] (with value [%s]) found in JSON but could not stub. Could be its name or value type (%s) doesn't match methods in %s", key, obj.get(key), getterTypeDef, proxy.getType()));
						} else {
							proxy.addValueStub(ProxyUtils.getterName(key), stub);
						}
					} catch (JSONException e) {
						throw convert(e);
					}
				}
			}
		};
	}
}
