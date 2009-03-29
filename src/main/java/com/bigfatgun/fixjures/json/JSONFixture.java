package com.bigfatgun.fixjures.json;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bigfatgun.fixjures.FixtureHandler;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.FixtureBuilder;
import com.bigfatgun.fixjures.SourcedFixtureBuilder;
import com.bigfatgun.fixjures.handlers.NumberFixtureHandler;
import com.bigfatgun.fixjures.handlers.BooleanFixtureHandler;
import com.bigfatgun.fixjures.handlers.StringFixtureHandler;
import static com.bigfatgun.fixjures.Fixjure.warn;
import com.bigfatgun.fixjures.mock.MockHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import org.jmock.Mock;
import org.jmock.core.Stub;
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
public final class JSONFixture extends FixtureSource {

	/**
	 * Loads all text from a file.
	 *
	 * @param file file to load
	 * @return text in file, null if IO error occurs
	 */
	private static String loadTextFromFile(final File file) {
		try {
			final RandomAccessFile raf = new RandomAccessFile(file, "r");
			final ByteBuffer buf = ByteBuffer.allocate(Short.MAX_VALUE);
			final Charset cset = Charset.forName("UTF-8");
			final CharsetDecoder decoder = cset.newDecoder();
			final StringBuilder string = new StringBuilder((int) Math.min(Integer.MAX_VALUE, file.length()) / 2);
			final FileChannel channel = raf.getChannel();

			try {
				while (channel.read(buf) != -1) {
					buf.flip();
					string.append(decoder.decode(buf));
					buf.clear();
				}
			} finally {
				channel.close();
			}

			return string.toString();
		} catch (IOException e) {
			warn("Could not load JSON fixture data: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Raw JSON.
	 */
	private final String rawJson;

	/**
	 * Map of json value type to fixture handler.
	 */
	private final IdentityHashMap<Class, FixtureHandler> jsonValueFixtureHandlers;

	/**
	 * Map of fixture handlers.
	 */
	private ImmutableMap<Class, FixtureHandler> fixtureHandlers;

	/**
	 * @param raw raw JSON
	 */
	public JSONFixture(final String raw) {
		rawJson = raw;
		jsonValueFixtureHandlers = Maps.newIdentityHashMap();
		installDefaultHandlers();
	}

	/**
	 * @param jsonFile file with JSON
	 */
	public JSONFixture(final File jsonFile) {
		this(loadTextFromFile(jsonFile));
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
	@SuppressWarnings({ "unchecked" })
	public <T> T createFixture(final Class<? super T> type) {
		try {
			final Object obj = findValue(type, new JSONObject(rawJson), "ROOT");
			if (type.isAssignableFrom(obj.getClass())) {
				return (T) obj;
			} else {
				warn("Invalid class! Expect " + type + " but got " + obj.getClass());
				return null;
			}
		} catch (JSONException e) {
			warn("JSON error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * @param type object type
	 * @param value object value
	 * @param name object name
	 * @return proxied object
	 * @throws JSONException if there is an error loading JSON data
	 */
	private Object findValue(final Type type, final Object value, final String name) throws JSONException {
		final Class< ? > getterClass;
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
	private Object findValue(final Class< ? > type, final Type[] typeParams, final Object value, final String name) throws JSONException {
		if (fixtureHandlers.containsKey(type)) {
			//noinspection unchecked
			return fixtureHandlers.get(type).deserialize(type, value, name);
		}

		for (final Class< ? > key : jsonValueFixtureHandlers.keySet()) {
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
		} else if (JSONArray.class.isAssignableFrom(value.getClass())) {
			final JSONArray array = (JSONArray) value;
			if (!type.isArray() && typeParams == null) {
				warn("Only generic collections or arrays are supported, failed to stub " + name + " in " + type);
				return null;
			} else if (type.isArray()) {
				final Class< ? > collectionType = type.getComponentType();
				final Object actualArray = Array.newInstance(collectionType, array.length());
				for (int i = 0; i < array.length(); i++) {
					Array.set(actualArray, i, findValue(collectionType, array.get(i), name + "[" + i + "]"));
				}
				return actualArray;
			} else {
				final Multiset source = LinkedHashMultiset.create();
				final Class< ? > collectionType = (Class< ? >) typeParams[0];

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
				} else if (Multiset.class.isAssignableFrom(type)) {
					//noinspection unchecked
					return ImmutableMultiset.copyOf(source);
				} else {
					warn("Don't know what to do with collection of type " + collectionType);
					return null;
				}
			}
		} else {
			warn("Value type not yet supported: " + value.getClass().getName());
			return null;
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
		if (cls.isInterface()) {
			return proxyJSONObject(cls, jsonObject);
		} else {
			return instantiateJSONObject(cls, jsonObject);
		}
	}

	/**
	 * Instantiates an object and invokes setters to set values.
	 *
	 * @param cls type of object
	 * @param jsonObject json value
	 * @param <T> type of object
	 * @return instantiated object
	 * @throws JSONException if there is a JSON error
	 */
	private <T> T instantiateJSONObject(final Class<T> cls, final JSONObject jsonObject) throws JSONException {
		final T object;
		try {
			final Constructor<T> ctor = cls.getDeclaredConstructor();
			if (!ctor.isAccessible()) {
				ctor.setAccessible(true);
			}
			object = ctor.newInstance();
		} catch (Exception e) {
			warn("Error instantiating object of type: " + cls);
			e.printStackTrace();
			return null;
		}

		for (final Iterator objIterator = jsonObject.keys(); objIterator.hasNext();) {
			final String key = objIterator.next().toString();
			setInstanceValue(cls, object, key, jsonObject.get(key));
		}

		return object;
	}

	/**
	 * Converts JSON to an object.
	 *
	 * @param cls object type
	 * @param jsonObject json
	 * @param <T> object type
	 * @return converted object
	 * @throws JSONException if there is an error with the JSON
	 */
	private <T> T proxyJSONObject(final Class<T> cls, final JSONObject jsonObject) throws JSONException {
		final Mock mock = new Mock(cls);
		for (final Iterator objIterator = jsonObject.keys(); objIterator.hasNext();) {
			final String key = objIterator.next().toString();
			final Stub stub = getterValueStub(cls, key, jsonObject.get(key));
			if (stub == null) {
				warn("Key [" + key + "] found in JSON but could not stub. Could be its name or value type doesn't match methods in " + cls);
			} else {
				mock.stubs().method(getterName(key).toString()).will(stub);
			}
		}
		return cls.cast(mock.proxy());
	}

	/**
	 * Sets a bean value.
	 * @param cls type of bean
	 * @param object bean instance
	 * @param key value key
	 * @param value value
	 * @param <T> bean type
	 * @throws JSONException if there is bad JSON
	 */
	private <T> void setInstanceValue(final Class<T> cls, final T object, final String key, final Object value) throws JSONException {
		try {
			final Method getter = cls.getMethod(getterName(key).toString());
			final Method setter = cls.getMethod(setterName(key).toString(), getter.getReturnType());

			setter.invoke(object, findValue(getter.getGenericReturnType(), value, getterName(key).toString()));
		} catch (NoSuchMethodException e) {
			warn("No getter and setter found in " + cls + " for " + key);
		} catch (Exception e) {
			warn("Exception while attempting setter in " + cls + " for " + key);
		}
	}

	private CharSequence getterName(final String propertyName) {
		final StringBuilder builder = new StringBuilder("get");
		builder.append(Character.toUpperCase(propertyName.charAt(0)));
		builder.append(propertyName.substring(1));
		return builder;
	}

	private CharSequence setterName(final String propertyName) {
		final StringBuilder builder = new StringBuilder("set");
		builder.append(Character.toUpperCase(propertyName.charAt(0)));
		builder.append(propertyName.substring(1));
		return builder;
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
	private Stub getterValueStub(final Class parentCls, final String keyName, final Object value) throws JSONException {
		final String getterName = getterName(keyName).toString();
		final Method getter;
		try {
			getter = parentCls.getMethod(getterName);
		} catch (NoSuchMethodException e) {
			return null;
		}
		return MockHelper.returnValue(findValue(getter.getGenericReturnType(), value, getterName));
	}

	@Override
	public <T> SourcedFixtureBuilder<T> build(final FixtureBuilder<T> builder) {
		return new SourcedFixtureBuilder<T>(builder) {
			@Override
			protected Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> handlers) {
				JSONFixture.this.fixtureHandlers = handlers;
				try {
					return JSONFixture.this.<T>createFixture(getType());
				} finally {
					close();
				}
			}
		};
	}

	@Override
	public void close() {
		// do nothing and don't throw exception
	}
}
