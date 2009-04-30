package com.bigfatgun.fixjures.annotations;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

/**
 * Interface implemented by the source type enumeration.
 *
 * @author Steve Reed
 */
public interface SourceType {

	/**
	 * Opens a stream.
	 *
	 * @param location source
	 * @return stream
	 * @throws IOException if there is an error getting source data
	 */
	ReadableByteChannel openStream(String location) throws IOException;
}
