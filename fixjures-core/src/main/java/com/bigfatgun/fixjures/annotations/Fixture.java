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
	 * @return fixture source
	 */
	String value();

	/**
	 * @return fixture source format, default is {@link com.bigfatgun.fixjures.annotations.NativeSourceFormat#Json}
	 * if not specified
	 */
	NativeSourceFormat format() default NativeSourceFormat.Json;

	/**
	 * @return fixture source type, default is {@link com.bigfatgun.fixjures.annotations.NativeSourceType#Literal}
	 * if not specified
	 */
	NativeSourceType type() default NativeSourceType.Literal;
}
