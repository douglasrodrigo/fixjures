package com.bigfatgun.fixjures;

import java.lang.reflect.Method;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.bigfatgun.fixjures.annotations.NativeSourceFormat;

/**
 * Injects fixtures into objects annotated with the {@literal @Fixture} annotation.
 *
 * @author Steve Reed
 */
public class FixtureInjector {

	public static void scan(final Object obj) throws Exception {
		final Class cls = obj.getClass();
		for (final Method m : cls.getMethods()) {
         if (m.isAnnotationPresent(Fixture.class)) {
				// found one
				// want a signature with one argument, matching annotation type
				// and no return code?
				final Class[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 1 && m.getReturnType() == Void.TYPE) {
					// we're cool
					final Fixture fixture = m.getAnnotation(Fixture.class);
					//noinspection unchecked
					m.invoke(obj, newFactory(cls.getClassLoader(), fixture).createFixture(paramTypes[0], fixture.name()));
				}
			}
      }
	}

	private static FixjureFactory newFactory(final ClassLoader clsLoader, final Fixture fixtureAnnotation) {
		final NativeSourceFormat fmt = fixtureAnnotation.format();
		final SourceFactory fact = fmt.createSourceFactory(clsLoader, fixtureAnnotation);
		return FixjureFactory.newFactory(fact);
	}
}
