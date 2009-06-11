package com.bigfatgun.fixjures.annotations;

import com.bigfatgun.fixjures.SourceFactory;

/**
 * Interface implemented by the native source format enumeration.
 *
 * @author Steve Reed
 */
public interface SourceFormat {

	/**
	 * Creates a source factory based on a fixture annotation.
	 *
	 * @param fixtureClassLoader class loader of fixture requestor
	 * @param sourceType fixture source type
	 * @return new source factory
	 */
	SourceFactory createSourceFactory(ClassLoader fixtureClassLoader, SourceType sourceType);
}
