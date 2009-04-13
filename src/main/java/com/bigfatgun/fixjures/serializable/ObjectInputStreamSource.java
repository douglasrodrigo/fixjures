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
package com.bigfatgun.fixjures.serializable;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.FixtureStream;
import com.google.common.collect.ImmutableList;

/**
 * This fixture source implements {@link com.bigfatgun.fixjures.FixtureStream}, meaning
 * it can be used to provide multiple fixture objects in its lifetime.
 * <p>
 * It lazily creates an {@code ObjectInputStream} from the underlying
 * {@code ReadableByteChannel} during the first call to
 * {@link #createFixture(Class, com.google.common.collect.ImmutableList)} and does not close it until
 * {@link #close()} is invoked.
 */
public class ObjectInputStreamSource extends FixtureSource implements FixtureStream {

	public static ObjectInputStreamSource newFile(final File file) throws FileNotFoundException {
		if (file == null) {
			throw new NullPointerException("file");
		}
		return new ObjectInputStreamSource(new RandomAccessFile(file, "r").getChannel());
	}

	public static ObjectInputStreamSource newObjectInputStream(final ReadableByteChannel channel) {
		return new ObjectInputStreamSource(channel);
	}

	private ObjectInputStream objIn;

	/**
	 * Initializes the source.
	 *
	 * @param source source data
	 */
	ObjectInputStreamSource(final ReadableByteChannel source) {
		super(source);
	}

	/**
	 * Lazy-creates an {@code ObjectInputStream} and reads an object from it.
	 *
	 * @param type fixture object type
	 * @param typeParams type params
	 * @param <T> fixture object type
	 * @return new fixture object
	 */
	@Override
	public <T> T createFixture(final Class<T> type, final ImmutableList<Class<?>> typeParams) {
		try {
			if (objIn == null) {
				objIn = new ObjectInputStream(Channels.newInputStream(getSource()));
			}
			return type.cast(objIn.readObject());
		} catch (IOException e) {
			throw new FixtureException(e);
		} catch (ClassNotFoundException e) {
			throw new FixtureException(e);
		}
	}

	/**
	 * Closes the {@code ObjectInputStream} and then calls {@link com.bigfatgun.fixjures.FixtureSource#close()}.
	 *
	 * @throws IOException if there is any error closing the io streams
	 */
	@Override
	public void close() throws IOException {
		objIn.close();
		super.close();
	}

	public FixtureSource asSourceStream() {
		return this;
	}
}
