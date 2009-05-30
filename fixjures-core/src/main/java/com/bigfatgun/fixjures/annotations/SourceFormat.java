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
	 * @param clsLoader class loader of fixture requestor
	 * @param fixtureAnnotation fixture annotation
	 * @return new source factory
	 */
	SourceFactory createSourceFactory(ClassLoader clsLoader, Fixture fixtureAnnotation);
}
