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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bigfatgun.fixjures.Fixjure.Option.SKIP_UNMAPPABLE;
import com.bigfatgun.fixjures.FixtureBuilder;
import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.SourcedFixtureBuilder;
import com.bigfatgun.fixjures.proxy.ConcreteReflectionProxy;
import com.bigfatgun.fixjures.proxy.InterfaceProxy;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
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
	 * An enumeration of source types.
	 */
	public static enum SourceType {
		/** String literal. */
		LITERAL,
		/** File. */
		FILE
	}

	/**
	 * @param jsonFile file with JSON
	 * @throws FileNotFoundException if the file does not exist
	 */
	public JSONSource(final File jsonFile) throws FileNotFoundException {
		this(new RandomAccessFile(jsonFile, "r").getChannel());
	}

	/**
	 * @param input input stream with data
	 */
	public JSONSource(final InputStream input) {
		this(Channels.newChannel(input));
	}

	/**
	 * Creates a new JSON source from the given {@code ReadableByteChannel}. The channel isn't read
	 * until the fixture object is created.
	 *
	 * @param source byte source
	 */
	public JSONSource(final ReadableByteChannel source) {
		super(source);
	}

	/**
	 * @param raw raw JSON
	 */
	public JSONSource(final String raw) {
		this(new ByteArrayInputStream(getBytes(raw)));
	}

	/**
	 * @param url url with json
	 * @throws IOException if there is an error retrieving data at url
	 */
	public JSONSource(final URL url) throws IOException {
		this(url.openStream());
	}

	/**
	 * Builds a new {@link com.bigfatgun.fixjures.json.JSONSourcedFixtureBuilder}.
	 *
	 * @param builder the builder to convert
	 * @param <T> fixture object type
	 * @return json-sourced fixture builder
	 */
	@Override
	public <T> SourcedFixtureBuilder<T, JSONSource> build(final FixtureBuilder<T> builder) {
		return new JSONSourcedFixtureBuilder<T>(this, builder);
	}

	/**
	 * @param type type of object to proxy
	 * @param typeParams type params
	 * @return proxied object
	 */
	public <T> T createFixture(final Class<? extends T> type, final List<Class<?>> typeParams) {
		try {
			final String sourceJson = loadTextFromChannel(getSource());
			Object rawValue;
			final String sourceJsonTrimmed = sourceJson.trim();
			if (sourceJsonTrimmed.startsWith("{")) {
				rawValue = new JSONObject(sourceJsonTrimmed);
			} else if (sourceJsonTrimmed.startsWith("[")) {
				rawValue = new JSONArray(sourceJsonTrimmed);
			} else {
				try {
					rawValue = Double.parseDouble(sourceJsonTrimmed);
				} catch (Exception e1) {
					rawValue = String.valueOf(sourceJsonTrimmed);
				}
			}

			return (T) findValue(type, typeParams, rawValue, "ROOT");
		} catch (FixtureException e) {
			throw e;
		} catch (Exception e) {
			throw new FixtureException(e);
		}
	}

	/**
	 * Converts JSONObjects and JSONArrays into appropriate values.
	 *
	 * @param requiredType required type
	 * @param typeParams required type params, never null
	 * @param sourceValue source value
	 * @param name value name, for logging
	 * @return converted value
	 */
	@Override
	protected Object handle(final Class requiredType, final List<? extends Type> typeParams, final Object sourceValue, final String name) {
		try {
			if (JSONObject.class.isAssignableFrom(sourceValue.getClass())) {
				final JSONObject jsonObj = (JSONObject) sourceValue;
				if (Map.class.isAssignableFrom(requiredType)) {
					ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
					for (final Iterator i = jsonObj.keys(); i.hasNext();) {
						final Object lookupKey = i.next();
						Object key = findValue(typeParams.size() > 0 ? typeParams.get(0) : Object.class, null, lookupKey, "key");
						Object keyValue = findValue(typeParams.size() > 1 ? typeParams.get(1) : Object.class, null, jsonObj.get(String.valueOf(lookupKey)), "sourceValue");
						builder = builder.put(key, keyValue);
					}
					return Maps.newHashMap(builder.build());
				} else {
					//noinspection unchecked
					return generateObject(requiredType, typeParams, (JSONObject) sourceValue);
				}
			} else if (JSONArray.class.isAssignableFrom(sourceValue.getClass())) {
				final JSONArray array = (JSONArray) sourceValue;
				if (requiredType.isArray()) {
					final Class collectionType = requiredType.getComponentType();
					final Object actualArray = Array.newInstance(collectionType, array.length());
					for (int i = 0; i < array.length(); i++) {
						Array.set(actualArray, i, findValue(collectionType, Arrays.<Type>asList(), array.get(i), name + "[" + i + "]"));
					}
					return actualArray;
				} else {
					final Multiset source = LinkedHashMultiset.create();
					final Type collectionType = typeParams.size() > 0
						? typeParams.get(0)
						: Object.class;

					for (int i = 0; i < array.length(); i++) {
						//noinspection unchecked
						source.add(findValue(collectionType, null, array.get(i), name + "[" + i + "]"));
					}

					if (List.class.isAssignableFrom(requiredType)) {
						//noinspection unchecked
						return Lists.newArrayList(source);
					} else if (Set.class.isAssignableFrom(requiredType)) {
						//noinspection unchecked
						return Sets.newHashSet(source);
					} else if (Multiset.class.isAssignableFrom(requiredType)) {
						//noinspection unchecked
						return source;
					} else {
						throw new FixtureException("Unhandled destination requiredType: " + requiredType);
					}
				}
			} else if (Object.class.equals(requiredType)) {
				return sourceValue;
			} else {
				throw new FixtureException("Could not convert source value " + sourceValue + " to type " + requiredType);
			}
		} catch (JSONException e) {
			throw new FixtureException(e);
		}
	}

	/**
	 * Creates a proxy object based on the class. If the class is an interface, a
	 * {@link com.bigfatgun.fixjures.proxy.InterfaceProxy} is returned, otherwise a
	 * {@link com.bigfatgun.fixjures.proxy.ConcreteReflectionProxy} is assumed to be the appropriate
	 * proxy.
	 *
	 * @param cls proxy object type
	 * @param <T> proxy object type
	 * @return object proxy
	 */
	private <T> ObjectProxy<T> createObjectProxy(final Class<T> cls) {
		if (cls.isInterface()) {
			return new InterfaceProxy<T>(cls);
		} else {
			return new ConcreteReflectionProxy<T>(cls);
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
	private <T> void configureProxy(final ObjectProxy<T> proxy, final List<? extends Type> typeParams, final JSONObject obj) {
		final Iterator objIterator = obj.keys();

		for (int i = 0; objIterator.hasNext(); i++) {
			final String key = objIterator.next().toString();
			final Type type = (typeParams.size() <= i) ? null : typeParams.get(i);
			try {
				final Object stub = findValue(proxy.getType(), type, key, obj.get(key));
				if (stub == null) {
					if (isOptionEnabled(SKIP_UNMAPPABLE)) {
						continue;
					}

					throw new FixtureException(String.format("Key [%s] found in JSON but could not stub. Could be its name or value type doesn't match methods in %s", key, proxy.getType()));
				} else {
					proxy.addValueStub(getterName(key), stub);
				}
			} catch (JSONException e) {
				throw new FixtureException(e);
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
	private Object findValue(final Class parentCls, final Type probableType, final String keyName, final Object value) {
		final String getterName = getterName(keyName);
		final Method getter;
		try {
			getter = parentCls.getMethod(getterName);
			return findValue(getter.getGenericReturnType(), probableType, value, getterName);
		} catch (NoSuchMethodException e) {
			if (isOptionEnabled(SKIP_UNMAPPABLE)) {
				return null;
			} else {
				throw new FixtureException(e);
			}
		}
	}

	/**
	 * Generates an object either by creating a proxy of an interfce, or by instantiating it.
	 *
	 * @param cls type of object
	 * @param typeParams type params
	 * @param jsonObject json value
	 * @param <T> type of object
	 * @return instantiated or proxied object
	 */
	private <T> T generateObject(final Class<T> cls, final List<? extends Type> typeParams, final JSONObject jsonObject) {
		final ObjectProxy<T> proxy = createObjectProxy(cls);
		configureProxy(proxy, typeParams, jsonObject);
		try {
			return proxy.create();
		} catch (Exception e) {
			throw new FixtureException(e);
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
