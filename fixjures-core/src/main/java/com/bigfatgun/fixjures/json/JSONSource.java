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
package com.bigfatgun.fixjures.json;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bigfatgun.fixjures.Fixjure;
import static com.bigfatgun.fixjures.Fixjure.Option.SKIP_UNMAPPABLE;
import com.bigfatgun.fixjures.FixtureException;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.FixtureTypeDefinition;
import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;
import static com.bigfatgun.fixjures.FixtureTypeDefinition.newDefinition;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.Proxies;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mocks objects based on JSON fixture data.
 * <p/>
 * Date: Mar 25, 2009
 * <p/>
 * Time: 8:48:25 AM
 *
 * @author Steve Reed
 */
public final class JSONSource extends FixtureSource {

	/**
	 * Static factory for stream source.
	 *
	 * @param channel source channel
	 * @return new fixture source
	 */
	public static FixtureSource newJsonStream(final ReadableByteChannel channel) {
		return new JSONSource(channel);
	}

	/**
	 * Static factory for file source.
	 *
	 * @param jsonFile json file
	 * @return new json source
	 * @throws FileNotFoundException if file is not found
	 */
	public static FixtureSource newJsonFile(final File jsonFile) throws FileNotFoundException {
		return new JSONSource(jsonFile);
	}

	/**
	 * Static factory for resource-name based source.
	 *
	 * @param resourceName name of resource to load from class loader
	 * @return new json resource
	 * @throws FileNotFoundException if the resource is not found
	 */
	public static FixtureSource newJsonResource(final String resourceName) throws FileNotFoundException {
		return newJsonResource(FixtureSource.class.getClassLoader(), resourceName);
	}

	/**
	 * Static factory for resource-name based source.
	 *
	 * @param clsLoader classloader to use to locate resource
	 * @param resourceName resource to load
	 * @return new json resource
	 * @throws FileNotFoundException if the resource is not found
	 */
	public static FixtureSource newJsonResource(final ClassLoader clsLoader, final String resourceName) throws FileNotFoundException {
		final InputStream input = clsLoader.getResourceAsStream(resourceName);
		if (input == null) {
			throw new FileNotFoundException(resourceName);
		} else {
			return new JSONSource(input);
		}
	}

	/**
	 * Static factory for a json string literal.
	 *
	 * @param json json string literal
	 * @return new json source
	 */
	public static FixtureSource newJsonString(final String json) {
		return new JSONSource(json);
	}

	/**
	 * Static factory for URL source.
	 *
	 * @param url json url
	 * @return new json source
	 * @throws IOException if json cannot be found/retrieved at the given url
	 */
	public static FixtureSource newRemoteUrl(final URL url) throws IOException {
		return new JSONSource(url);
	}

	private JSONSource(final File jsonFile) throws FileNotFoundException {
		this(new RandomAccessFile(jsonFile, "r").getChannel());
	}

	private JSONSource(final InputStream input) {
		this(Channels.newChannel(input));
	}

	private JSONSource(final ReadableByteChannel source) {
		super(source);
	}

	private JSONSource(final String raw) {
		this(new ByteArrayInputStream(getBytes(raw)));
	}

	private JSONSource(final URL url) throws IOException {
		this(url.openStream());
	}

	public <T> T createFixture(final FixtureTypeDefinition<T> type) {
		try {
			final String sourceJson = loadSource();
			final Object jsonValue = parseJson(sourceJson);
			final ValueProvider<?> provider = findValue(type, jsonValue, "ROOT");
			final Object value = provider.get();
			return (value == null) ? null : type.getType().cast(value);
		} catch (Exception e) {
			throw convert(e);
		}
	}

	private Object parseJson(final String json) throws JSONException {
		assert json != null : "JSON data cannot be null.";
		if (json.startsWith("{")) {
			return new JSONObject(json);
		} else if (json.startsWith("[")) {
			return new JSONArray(json);
		} else {
			return tryToParseStringToNumber(json);
		}
	}

	private Object tryToParseStringToNumber(final String string) {
		try {
			return Double.parseDouble(string);
		} catch (Exception e) {
			return string;
		}
	}

	private String loadSource() throws IOException {
		final String untrimmed = loadTextFromChannel(getSource());
		return untrimmed.trim();
	}

	/**
	 * Converts JSONObjects and JSONArrays into appropriate values.
	 *
	 * @param requiredType required type
	 * @param sourceValue source value
	 * @param name value name, for logging
	 * @return converted value
	 */
	@Override
	protected ValueProvider<?> handle(final FixtureTypeDefinition<?> requiredType, final Object sourceValue, final String name) {
		// todo : ugly! start here!!!
		final ValueProvider<?> fromSuper = super.handle(requiredType, sourceValue, name);
		if (fromSuper != null) {
			return fromSuper;
		}

		try {
			if (JSONObject.class.isAssignableFrom(sourceValue.getClass())) {
				final JSONObject jsonObj = (JSONObject) sourceValue;
				if (Map.class.isAssignableFrom(requiredType.getType())) {
					ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
					for (final Iterator i = jsonObj.keys(); i.hasNext();) {
						final Object lookupKey = i.next();
						Object key = findValue(requiredType.keyType(), null, lookupKey, "key").get();
						Object keyValue = findValue(requiredType.valueType(), null, jsonObj.get(String.valueOf(lookupKey)), "sourceValue").get();
						if (key != null && keyValue != null) {
							builder = builder.put(key, keyValue);
						}
					}
					return ValueProviders.of(Maps.newHashMap(builder.build()));
				} else {
					return ValueProviders.of(newConfiguredProxy(requiredType, (JSONObject) sourceValue));
				}
			} else if (JSONArray.class.isAssignableFrom(sourceValue.getClass())) {
				final JSONArray array = (JSONArray) sourceValue;
				if (requiredType.getType().isArray()) {
					final Class<?> collectionType = requiredType.getType().getComponentType();
					final Object actualArray = Array.newInstance(collectionType, array.length());
					for (int i = 0; i < array.length(); i++) {
						Array.set(actualArray, i, findValue(newDefinition(collectionType), array.get(i), name + "[" + i + "]").get());
					}
					return ValueProviders.of(actualArray);
				} else {
					final Multiset<Object> source = LinkedHashMultiset.create();
					final Type collectionType = requiredType.collectionType();

					for (int i = 0; i < array.length(); i++) {
						source.add(findValue(collectionType, null, array.get(i), name + "[" + i + "]").get());
					}

					if (requiredType.isA(List.class)) {
						return ValueProviders.of(Lists.newArrayList(source));
					} else if (requiredType.isA(Set.class)) {
						return ValueProviders.of(Sets.newHashSet(source));
					} else if (requiredType.isA(Multiset.class)) {
						return ValueProviders.of(source);
					} else {
						throw new FixtureException("Unhandled destination requiredType: " + requiredType);
					}
				}
			} else if (Object.class.equals(requiredType.getType())) {
				return ValueProviders.of(sourceValue);
			} else {
				throw new FixtureException("Could not convert source value " + sourceValue + " to type " + requiredType);
			}
		} catch (JSONException e) {
			throw convert(e);
		}
	}

	/**
	 * Configures an object proxy to return the values in the given JSON object.
	 *
	 * @param proxy proxy to configure
	 * @param typeParams type params
	 * @param obj values to return
	 * @param <T> proxy object type
	 */
	private <T> void configureProxy(final ObjectProxy<T> proxy, final ImmutableList<? extends Type> typeParams, final JSONObject obj) {
		final Iterator objIterator = obj.keys();
		// TODO : round here is where to test for lazy eval
		for (int i = 0; objIterator.hasNext(); i++) {
			final String key = objIterator.next().toString();
			final Type type = (typeParams.size() <= i) ? null : typeParams.get(i);
			try {
				final ValueProvider<?> stub;
				if (isOptionEnabled(Fixjure.Option.LAZY_REFERENCE_EVALUATION)) {
					stub = new ValueProvider<Object>() {
						@Override
						public Object get() {
							try {
								return findValue(proxy.getType(), type, key, obj.get(key)).get();
							} catch (JSONException e) {
								throw convert(e);
							}
						}
					};
				} else {
					stub = findValue(proxy.getType(), type, key, obj.get(key));
				}

				if (stub == null) {
					if (isOptionEnabled(SKIP_UNMAPPABLE)) {
						continue;
					}

					throw new FixtureException(String.format("Key [%s] found in JSON but could not stub. Could be its name or value type doesn't match methods in %s", key, proxy.getType()));
				} else {
					proxy.addValueStub(getterName(key), stub);
				}
			} catch (JSONException e) {
				throw convert(e);
			}
		}
	}

	/**
	 * Finds a value based on type info for the corresponding getter method.
	 *
	 * @param parentCls containing class
	 * @param probableType if not null, could indicate required value type
	 * @param keyName json key name
	 * @param value json value
	 * @return value stub
	 */
	private ValueProvider<?> findValue(final Class<?> parentCls, final Type probableType, final String keyName, final Object value) {
		final String getterName = getterName(keyName);
		final Method getter;
		try {
			getter = parentCls.getMethod(getterName);
			return findValue(getter.getGenericReturnType(), probableType, value, getterName);
		} catch (NoSuchMethodException e) {
			if (isOptionEnabled(SKIP_UNMAPPABLE)) {
				return null;
			} else {
				throw convert(e);
			}
		}
	}

	/**
	 * Generates an object either by creating a proxy of an interfce, or by instantiating it.
	 *
	 * @param cls type of object
	 * @param jsonObject json value
	 * @return instantiated or proxied object
	 */
	private Object newConfiguredProxy(final FixtureTypeDefinition<?> cls, final JSONObject jsonObject) {
		final ObjectProxy<?> proxy = Proxies.newProxy(cls.getType());
		configureProxy(proxy, cls.getParams(), jsonObject);
		try {
			return proxy.create();
		} catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Converts a property name from JSON into a getter name. For example, "firstName" will be transformed into
	 * "getFirstName".
	 *
	 * @param propertyName property name
	 * @return getter name
	 */
	private String getterName(final String propertyName) {
		final StringBuilder builder = new StringBuilder("get");
		builder.append(Character.toUpperCase(propertyName.charAt(0)));
		builder.append(propertyName.substring(1));
		return builder.toString();
	}
}
