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
	public static String loadTextFromChannel(final ReadableByteChannel channel, final Charset charset) throws IOException {
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

			return string.toString();
		} finally {
			channel.close();
		}
	}
}
