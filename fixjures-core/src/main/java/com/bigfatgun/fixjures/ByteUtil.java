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

package com.bigfatgun.fixjures;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public final class ByteUtil {
	private ByteUtil() {}

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 * Converts the given string into a UTF-8 encoded byte array.
	 *
	 * @param str string to convert
	 * @return byte array
	 */
	public static byte[] getBytes(final String str) {
		return getBytes(str, DEFAULT_CHARSET);
	}

	public static byte[] getBytes(final String str, final Charset charset) {
		return str.getBytes(charset);
	}

	/**
	 * Reads the entire contents of the given byte channel into a string builder. The channel is still open after this
	 * method returns.
	 *
	 * @param channel channel to read, will NOT be closed before the method returns
	 * @param charset decoding to use, null to use default
	 * @return string contents of channel
	 * @throws java.io.IOException if there are any IO errors while reading or closing the given channel
	 */
	public static CharSequence loadTextFromChannel(final ReadableByteChannel channel, final Charset charset) throws IOException {
		final Charset actualCharset = (charset == null) ? DEFAULT_CHARSET : charset;
		try {
			final ByteBuffer buf = ByteBuffer.allocate(Short.MAX_VALUE);
			final CharsetDecoder decoder = actualCharset.newDecoder();
			final StringBuilder string = new StringBuilder();

			while (channel.read(buf) != -1) {
				buf.flip();
				string.append(decoder.decode(buf));
				buf.clear();
			}

			return string;
		} finally {
			channel.close();
		}
	}
}
