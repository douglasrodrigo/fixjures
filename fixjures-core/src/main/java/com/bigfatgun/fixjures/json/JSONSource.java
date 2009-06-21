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
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.bigfatgun.fixjures.FixtureException;
import static com.bigfatgun.fixjures.FixtureException.convert;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ByteUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mocks objects based on JSON fixture data.
 */
public final class JSONSource extends FixtureSource {

	public static FixtureSource newJsonStream(final ReadableByteChannel channel) {
		return new JSONSource(channel);
	}

	public static FixtureSource newJsonFile(final File jsonFile) throws FileNotFoundException {
		return new JSONSource(new RandomAccessFile(jsonFile, "r").getChannel());
	}

	public static FixtureSource newJsonResource(final String resourceName) throws FileNotFoundException {
		return newJsonResource(FixtureSource.class.getClassLoader(), resourceName);
	}

	public static FixtureSource newJsonResource(final ClassLoader clsLoader, final String resourceName) {
		final InputStream input = clsLoader.getResourceAsStream(resourceName);
		if (input == null) {
			throw new FixtureException("Unable to locate resource: " + resourceName);
		} else {
			return new JSONSource(input);
		}
	}

	public static FixtureSource newJsonString(final String json) {
		return new JSONSource(new ByteArrayInputStream(ByteUtil.getBytes(json)));
	}

	public static FixtureSource newRemoteUrl(final URL url) {
		try {
			return new JSONSource(url.openStream());
		} catch (IOException e) {
			throw FixtureException.convert(e);
		}
	}

	private JSONSource(final InputStream input) {
		this(Channels.newChannel(input));
	}

	private JSONSource(final ReadableByteChannel source) {
		super(source);
		installJsonHandlers();
	}

	private void installJsonHandlers() {
		installTypeHandler(JsonHandlers.newMapHandler());
		installTypeHandler(JsonHandlers.newArrayHandler());
		installTypeHandler(JsonHandlers.newListHandler());
		installTypeHandler(JsonHandlers.newSetHandler());
		installTypeHandler(JsonHandlers.newMultisetHandler());
		installTypeHandler(JsonHandlers.newObjectProxyHandler());
	}

	public Object createFixture(final FixtureType type) {
		try {
			final String sourceJson = loadSource();
			final Object jsonValue = parseJson(sourceJson);
			final ValueProvider<?> provider = findValue(type, jsonValue);
			final Object value = provider.get();
			return type.getType().cast(value);
		} catch (Exception e) {
			throw convert(e);
		}
	}

	private Object parseJson(final String json) {
		try {
			return tryToParseJson(json);
		} catch (JSONException e) {
			throw FixtureException.convert(e);
		}
	}

	private Object tryToParseJson(final String json) throws JSONException {
		assert json != null : "JSON data cannot be null.";
		if (looksLikeJsonNull(json)) {
			return null;
		} else if (looksLikeJsonMap(json)) {
			return new JSONObject(json);
		} else if (looksLikeJsonArray(json)) {
			return new JSONArray(json);
		} else {
			return parseJsonToNumberOrString(json);
		}
	}

	private boolean looksLikeJsonNull(final String json) {
		return "null".equalsIgnoreCase(json);
	}

	private boolean looksLikeJsonMap(final String json) {
		return json.startsWith("{");
	}

	private boolean looksLikeJsonArray(final String json) {
		return json.startsWith("[");
	}

	private Object parseJsonToNumberOrString(final String string) {
		try {
			return Long.parseLong(string);
		} catch (NumberFormatException longParseException) {
			try {
				return Double.parseDouble(string);
			} catch (NumberFormatException doubleParseException) {
				return string;
			}
		}
	}

	private String loadSource() throws IOException {
		final String untrimmed = ByteUtil.loadTextFromChannel(getSource(), getCharset());
		return untrimmed.trim();
	}
}
