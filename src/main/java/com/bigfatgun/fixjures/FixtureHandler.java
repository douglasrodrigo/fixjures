package com.bigfatgun.fixjures;

/**
 * Fixture handler plugin which can intercept object deserialization and provide its
 * own behavior during fixture instantiation.
 * <p/>
 * Date: Mar 25, 2009
 * <p/>
 * Time: 11:26:54 AM
 *
 * @param <SourceType> type of source object provided by FixtureSource
 * @param <ReturnType> type of object returned by this handler
 * @author Steve Reed
 */
public interface FixtureHandler<SourceType, ReturnType> {

	/**
	 * Returns the type of object created by this handler.
	 * @return the type of object created by this handler
	 */
	Class<? extends ReturnType> getType();

	/**
	 * Deserializes the given object from a fixture source object.
	 *
	 * @param desiredType type required by consumer
	 * @param rawValue raw value
	 * @param name property name
	 * @return value
	 */
	ReturnType deserialize(Class desiredType, SourceType rawValue, String name);
}
