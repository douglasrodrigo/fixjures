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
	 * @param fixtureAnnotation fixture annotation
	 * @return new source factory
	 */
	SourceFactory createSourceFactory(Fixture fixtureAnnotation);
}
