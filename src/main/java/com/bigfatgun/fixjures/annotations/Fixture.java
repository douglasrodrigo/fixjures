package com.bigfatgun.fixjures.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a setter, field or argument as a fixture.
 *
 * @author Steve Reed
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface Fixture {

	/**
	 * @return fixture name
	 */
	String name() default "";

	/**
	 * @return fixture source
	 */
	String value();

	/**
	 * @return fixture source format
	 */
	NativeSourceFormat format() default NativeSourceFormat.Json;

	/**
	 * @return fixture source type
	 */
	NativeSourceType type() default NativeSourceType.Literal;
}
