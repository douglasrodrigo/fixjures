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

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableMap;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

/**
 * Factory methods for various fixture source strategies.
 *
 * @author Steve Reed
 */
public final class Strategies {

	/** A strategy for finding source data for a fixture of a given type and name. */
	public static interface SourceStrategy {

		/**
		 * Produces a byte channel from which fixture data can be read for an object of the given type and name.
		 *
		 * @param type fixture object type
		 * @param name object name or id
		 * @return source data channel
		 * @throws IOException can be thrown when there is an error reading or finding source data
		 */
		ReadableByteChannel findStream(Class<?> type, String name) throws IOException;
	}

	/** A strategy for producing resource names where fixture data is stored for objects of a given type and name. */
	public static interface ResourceNameStrategy {

		/**
		 * Produces a name of a resource from which fixture data can be read for an object of the given type and name.
		 *
		 * @param type fixture object type
		 * @param name object name or id
		 * @return resource name
		 */
		String getResourceName(Class<?> type, String name);

	}

	/**
	 * The default name strategy for classpath sourced fixture factories.
	 * <p/>
	 * This strategy makes the factory look for fixtures in the classpath in "fixjures/<i>package.Class</i>/<i>name</i>.json".
	 */
	public static final ResourceNameStrategy DEFAULT_CLASSPATH_NAME_STRATEGY = newFormatStringStrategy("fixjures/%s/%s.json");

	/**
	 * Creates a new resource name strategy based on a format string that will be passed to {@code String.format} with
	 * {@code type.getName()} and {@code name} as the format arguments.
	 *
	 * @param formatStr format string, must be non-null
	 * @return resource name
	 */
	public static ResourceNameStrategy newFormatStringStrategy(final String formatStr) {
		checkNotNull(formatStr);

		return new ResourceNameStrategy() {
			public String getResourceName(final Class<?> type, final String name) {
				return String.format(formatStr, type.getName(), name);
			}
		};
	}

	/**
	 * This source strategy does a simple lookup of type and name in a map that stores fixture data in raw byte arrays. A
	 * defensive copy is made of the map argument, so only the fixtures in the map at the time this method is invoked can
	 * be created.
	 *
	 * @param mem map that holds fixture data in memory
	 * @return new source strategy
	 */
	public static SourceStrategy newInMemoryStrategy(final Map<Class<?>, Map<String, String>> mem) {
		final ImmutableMap<Class<?>, Map<String, String>> copy = ImmutableMap.copyOf(mem);
		return new SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				if (!copy.containsKey(type)) {
					throw new IOException("Data for " + type + " not found.");
				} else if (!copy.get(type).containsKey(name)) {
					throw new IOException("Data for " + type.getName() + " named " + name + " not found.");
				} else {
					final byte[] bytes = copy.get(type).get(name).getBytes();
					final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					return Channels.newChannel(bais);
				}
			}
		};
	}

	/**
	 * Creates a new source strategy that looks for fixture source data in the classpath based on {@link
	 * #DEFAULT_CLASSPATH_NAME_STRATEGY}.
	 *
	 * @return new source strategy
	 */
	public static SourceStrategy newClasspathStrategy() {
		return newClasspathStrategy(DEFAULT_CLASSPATH_NAME_STRATEGY);
	}

	/**
	 * Creates a new source strategy that looks for fixture source data within the fixture object type's classloader.
	 *
	 * @param nameStrategy resource name strategy, must be non-null
	 * @return new source strategy
	 * @throws NullPointerException if {@code nameStrategy} is null
	 */
	public static SourceStrategy newClasspathStrategy(final ResourceNameStrategy nameStrategy) {
		if (nameStrategy == null) {
			throw new NullPointerException("nameStrategy");
		}

		return new SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				final String resourceName = nameStrategy.getResourceName(type, name);

				if (resourceName == null) {
					throw new NullPointerException("resourceName");
				}

				final InputStream stream = type.getClassLoader().getResourceAsStream(resourceName);
				if (stream == null) {
					throw new FixtureException("Resource not found: " + resourceName);
				}

				return Channels.newChannel(stream);
			}
		};
	}

	/**
	 * Creates a new source strategy that looks for fixture source data in files based on {@link
	 * #DEFAULT_CLASSPATH_NAME_STRATEGY}.
	 *
	 * @return new source strategy
	 */
	public static SourceStrategy newFileStrategy() {
		return newFileStrategy(DEFAULT_CLASSPATH_NAME_STRATEGY);
	}

	/**
	 * Creates a new source strategy that looks for fixture source data in a file.
	 *
	 * @param nameStrategy resource name strategy, must be non-null
	 * @return new source strategy
	 * @throws NullPointerException if {@code nameStrategy} is null
	 */
	public static SourceStrategy newFileStrategy(final ResourceNameStrategy nameStrategy) {
		checkNotNull(nameStrategy);

		return new SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				return new RandomAccessFile(new File(nameStrategy.getResourceName(type, name)), "r").getChannel();
			}
		};
	}

	/**
	 * Creates a new source strategy that looks for fixture source data in a file.
	 *
	 * @param clsLoader class loader to use to find resources
	 * @param nameStrategy resource name strategy, must be non-null
	 * @return new source strategy
	 * @throws NullPointerException if {@code nameStrategy} is null
	 */
	public static SourceStrategy newResourceStrategy(final ClassLoader clsLoader, final ResourceNameStrategy nameStrategy) {
		checkNotNull(nameStrategy);

		return new SourceStrategy() {
			public ReadableByteChannel findStream(final Class<?> type, final String name) throws IOException {
				assert type != null : "Type cannot be null.";
				assert name != null : "Name cannot be null.";

				final String rsrcName = nameStrategy.getResourceName(type, name);
				final InputStream stream = clsLoader.getResourceAsStream(rsrcName);
				if (stream == null) {
					throw new FileNotFoundException(rsrcName);
				}
				return Channels.newChannel(stream);
			}
		};
	}
}
