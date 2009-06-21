package com.bigfatgun.fixjures;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import javax.annotation.Nullable;

public final class ByteUtil {
	private ByteUtil() {}

	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * Converts the given string into a UTF-8 encoded byte array.
	 *
	 * @param str string to convert
	 * @return byte array
	 */
	public static byte[] getBytes(final String str) {
		return getBytes(str, DEFAULT_CHARSET);
	}

	public static byte[] getBytes(final String str, final String charset) {
		try {
			return str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw FixtureException.convert("Could not encode string \"" + str + "\" in charset " + charset + ".", e);
		}
	}

	/**
	 * Reads the entire contents of the given byte channel into a string builder. The channel is
	 * still open after this method returns.
	 *
	 * @param channel channel to read, will NOT be closed before the method returns
	 * @param charset decoding to use, null to use default
	 * @return string contents of channel
	 * @throws java.io.IOException if there are any IO errors while reading or closing the given channel
	 */
	public static String loadTextFromChannel(final ReadableByteChannel channel, @Nullable final String charset) throws IOException {
		final String actualCharset = (charset == null) ? DEFAULT_CHARSET : charset;
		try {
			final ByteBuffer buf = ByteBuffer.allocate(Short.MAX_VALUE);
			final CharsetDecoder decoder = Charset.forName(actualCharset).newDecoder();
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
