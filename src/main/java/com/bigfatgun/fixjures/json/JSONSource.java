/*
 * Copyright (C) 2009 bigfatgun.com
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
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureBuilder;
import com.bigfatgun.fixjures.FixtureHandler;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.SourcedFixtureBuilder;
import com.bigfatgun.fixjures.handlers.BooleanFixtureHandler;
import com.bigfatgun.fixjures.handlers.NumberFixtureHandler;
import com.bigfatgun.fixjures.handlers.StringFixtureHandler;
import com.bigfatgun.fixjures.proxy.ConcreteReflectionProxy;
import com.bigfatgun.fixjures.proxy.InterfaceProxy;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.ValueStub;
import com.bigfatgun.fixjures.proxy.ValueStubImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
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
 * @author stever
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

	/** Charset to use when reading byte streams and channels. */
	private static final String CHARSET = "UTF-8";

	/**
	 * Reads the entire contents of the given byte channel into a string builder. The channel is
	 * still open after this method returns.
	 *
	 * @param channel channel to read, will NOT be closed before the method returns
	 * @return string contents of channel
	 * @throws IOException if there are any IO errors while reading or closing the given channel
	 */
	private static String loadTextFromChannel(final ReadableByteChannel channel) throws IOException {
		try {
			final ByteBuffer buf = ByteBuffer.allocate(Short.MAX_VALUE);
			final CharsetDecoder decoder = Charset.forName(CHARSET).newDecoder();
			final StringBuilder string = new StringBuilder();

			while (channel.read(buf) != -1) {
				buf.flip();
				string.append(decoder.decode(buf));
				buf.clear();
			}

			return string.toString();
		} finally {
			channel.close();
		}
	}

	/**
	 * Converts the given string into a UTF-8 encoded byte array.
	 *
	 * @param str string to convert
	 * @return byte array
	 */
	private static byte[] getBytes(final String str) {
		try {
			return str.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("JSONSource requires UTF-8.");
		}
	}

	/**
	 * Raw JSON.
	 */
	private final ReadableByteChannel jsonSource;

	/**
	 * Map of json value type to fixture handler.
	 */
	private final IdentityHashMap<Class, FixtureHandler> jsonValueFixtureHandlers;

	/**
	 * Map of fixture handlers.
	 */
	private ImmutableMap<Class, FixtureHandler> fixtureHandlers;

	/**
	 * Creates a new JSON source from the given {@code ReadableByteChannel}. The channel isn't read
	 * until the fixture object is created.
	 *
	 * @param source byte source
	 */
	public JSONSource(final ReadableByteChannel source) {
		jsonSource = source;
		jsonValueFixtureHandlers = Maps.newIdentityHashMap();
		installDefaultHandlers();
	}

	/**
	 * @param raw raw JSON
	 */
	public JSONSource(final String raw) {
		this(Channels.newChannel(new ByteArrayInputStream(getBytes(raw))));
	}

	/**
	 * @param jsonFile file with JSON
	 * @throws FileNotFoundException if the file does not exist
	 */
	public JSONSource(final File jsonFile) throws FileNotFoundException {
		this(new RandomAccessFile(jsonFile, "r").getChannel());
	}

	/**
	 * Install default fixture handlers.
	 */
	private void installDefaultHandlers() {
		jsonValueFixtureHandlers.put(Number.class, new NumberFixtureHandler());
		jsonValueFixtureHandlers.put(String.class, new StringFixtureHandler());
		jsonValueFixtureHandlers.put(Boolean.class, new BooleanFixtureHandler());
	}

	/**
	 * @param type type of object to proxy
	 * @param <T> type of object to proxy
	 * @return proxied object
	 */
	public <T> T createFixture(final Class<T> type) {
		try {
			final String sourceJson = loadTextFromChannel(jsonSource);
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

			//noinspection unchecked
			return (T) findValue(type, rawValue, "ROOT");
		} catch (Exception e) {
			Fixjure.LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param type object type
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 * @throws JSONException if there is an error loading JSON data
	 */
	private Object findValue(final Type type, final Object value, final String name) throws JSONException {
		final Class getterClass;
		final Type[] typeParams;
		if (type instanceof ParameterizedType) {
			//noinspection unchecked
			getterClass = (Class) ((ParameterizedType) type).getRawType();
			//noinspection unchecked
			typeParams = ((ParameterizedType) type).getActualTypeArguments();
		} else {
			//noinspection unchecked
			getterClass = (Class) type;
			typeParams = null;
		}
		return findValue(getterClass, typeParams, value, name);
	}

	/**
	 * @param type object type
	 * @param typeParams object type's type params
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 * @throws JSONException if there is an error reading JSON
	 */
	private Object findValue(final Class type, final Type[] typeParams, final Object value, final String name) throws JSONException {
		if (fixtureHandlers.containsKey(type)) {
			//noinspection unchecked
			return fixtureHandlers.get(type).deserialize(type, value, name);
		}

		for (final Class key : jsonValueFixtureHandlers.keySet()) {
			if (key.isAssignableFrom(value.getClass())) {
				//noinspection unchecked
				return jsonValueFixtureHandlers.get(key).deserialize(type, value, name);
			}
		}

		if (JSONObject.class.isAssignableFrom(value.getClass())) {
			final JSONObject jsonObj = (JSONObject) value;
			if (Map.class.isAssignableFrom(type)) {
				ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
				for (final Iterator i = jsonObj.keys(); i.hasNext();) {
					Object key = i.next();
					Object keyValue = jsonObj.get(String.valueOf(key));
					builder = builder.put(key, keyValue);
				}
				return builder.build();
			} else {
				return generateObject(type, (JSONObject) value);
			}
		} else { // if (JSONArray.class.isAssignableFrom(value.getClass())) {
			final JSONArray array = (JSONArray) value;
			if (!type.isArray() && typeParams == null) {
				Fixjure.LOGGER.warning(String.format("Only generic collections or arrays are supported, failed to stub %s in %s", name, type));
				return null;
			} else if (type.isArray()) {
				final Class collectionType = type.getComponentType();
				final Object actualArray = Array.newInstance(collectionType, array.length());
				for (int i = 0; i < array.length(); i++) {
					Array.set(actualArray, i, findValue(collectionType, array.get(i), name + "[" + i + "]"));
				}
				return actualArray;
			} else {
				final Multiset source = LinkedHashMultiset.create();
				final Class collectionType = (Class) typeParams[0];

				for (int i = 0; i < array.length(); i++) {
					//noinspection unchecked
					source.add(findValue(collectionType, array.get(i), name + "[" + i + "]"));
				}

				if (List.class.isAssignableFrom(type)) {
					//noinspection unchecked
					return ImmutableList.copyOf(source);
				} else if (Set.class.isAssignableFrom(type)) {
					//noinspection unchecked
					return ImmutableSet.copyOf(source);
				} else { // if (Multiset.class.isAssignableFrom(type)) {
					//noinspection unchecked
					return ImmutableMultiset.copyOf(source);
//				} else {
//					warn("Don't know what to do with collection of type " + collectionType);
//					return null;
				}
			}
		}
	}

	/**
	 * Generates an object either by creating a proxy of an interfce, or by instantiating it.
	 *
	 * @param cls type of object
	 * @param jsonObject json value
	 * @param <T> type of object
	 * @return instantiated or proxied object
	 * @throws JSONException if there is bad JSON
	 */
	private <T> T generateObject(final Class<T> cls, final JSONObject jsonObject) throws JSONException {
		final ObjectProxy<T> proxy = createObjectProxy(cls);
		configureProxy(proxy, jsonObject);
		return proxy.create();
	}

	private <T> void configureProxy(final ObjectProxy<T> proxy, final JSONObject obj) throws JSONException {
		for (final Iterator objIterator = obj.keys(); objIterator.hasNext();) {
			final String key = objIterator.next().toString();
			final ValueStub stub = getterValueStub(proxy.getType(), key, obj.get(key));
			if (stub == null) {
				Fixjure.LOGGER.warning(String.format("Key [%s] found in JSON but could not stub. Could be its name or value type doesn't match methods in %s", key, proxy.getType()));
			} else {
				proxy.addValueStub(getterName(key), stub);
			}
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

	/**
	 * Creates a jmock value stub.
	 *
	 * @param parentCls containing class
	 * @param keyName json key name
	 * @param value json value
	 * @return value stub
	 * @throws JSONException if there is a JSON related error
	 */
	private ValueStub getterValueStub(final Class parentCls, final String keyName, final Object value) throws JSONException {
		final String getterName = getterName(keyName);
		final Method getter;
		try {
			getter = parentCls.getMethod(getterName);
			return new ValueStubImpl(findValue(getter.getGenericReturnType(), value, getterName));
		} catch (NoSuchMethodException e) {
			return null;
		}
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
	 * Closes the {@code ReadableByteChannel}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		jsonSource.close();
	}

	/**
	 * Sets fixture handlers.
	 *
	 * @param handlers handlers
	 */
	public void setFixtureHandlers(final ImmutableMap<Class, FixtureHandler> handlers) {
		fixtureHandlers = handlers;
	}
}
