package com.bigfatgun.fixjures;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Injects fixtures into objects annotated with the {@literal @Fixture} annotation.
 *
 * @author Steve Reed
 */
public class FixtureInjector {

	/** Singleton instance. */
	private static final FixtureInjector INSTANCE = new FixtureInjector();

	/**
	 * An invokable target that performs an injection.
	 */
	private static interface FixtureInjectionTarget {
		/**
		 * Invokes the method on the target object.
		 *
		 * @param obj object to inject fixture into
		 * @throws FixtureException if there's any error in creating the fixture or performing the injection
		 */
		void invoke(final Object obj) throws FixtureException;
	}

	/**
	 * Creates stubs and passes them in as parameters to a method.
	 */
	private static class AnnotatedMethodParameterTarget implements FixtureInjectionTarget {

		private final Method m;

		private final LinkedHashMap<Fixture, Class<?>> fixtures;

		public AnnotatedMethodParameterTarget(final Method method, final LinkedHashMap<Fixture, Class<?>> annotations) {
			m = method;
			fixtures = annotations;
		}

		@Override
		public void invoke(final Object obj) throws FixtureException {
			final Iterable<Object> values = Iterables.transform(fixtures.entrySet(), new Function<Map.Entry<Fixture, Class<?>>, Object>() {
				@Override
				public Object apply(@Nullable final Map.Entry<Fixture, Class<?>> entry) {
					return newFactory(obj.getClass().getClassLoader(), entry.getKey()).createFixture(entry.getValue(), entry.getKey().value());
				}
			});
			try {
				m.invoke(obj, ImmutableList.copyOf(values).toArray());
			} catch (Exception e) {
				e.printStackTrace();
				throw new FixtureException(e);
			}
		}
	}

	private static class AnnotatedMethodTarget implements FixtureInjectionTarget {

		private final Fixture a;

		private final Method m;

		private final Class<?> t;

		public AnnotatedMethodTarget(final Method method, final Fixture fixture) {
			a = fixture;
			m = method;
			t = method.getParameterTypes()[0];
		}

		@Override
		public void invoke(final Object obj) {
			try {
				m.invoke(obj, newFactory(obj.getClass().getClassLoader(), a).createFixture(t, a.value()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new FixtureException(e);
			}
		}
	}

	/**
	 * Scans an object for methods annotated with {@code @Fixture} and attempts to create stubs
	 * to passed into them.
	 *
	 * @param obj object to scan
	 */
	public static void scan(final Object obj) {
		final ImmutableSet<FixtureInjectionTarget> targets = ImmutableSet.<FixtureInjectionTarget>builder()
				  .addAll(INSTANCE.findAnnotatedMethods(obj))
				  .addAll(INSTANCE.findAnnotatedMethodParameters(obj))
				  .build();

		for (final FixtureInjectionTarget target : targets) {
			target.invoke(obj);
		}
	}

	/**
	 * New factory from annotation.
	 *
	 * @param clsLoader class loader
	 * @param fixtureAnnotation fixture annotation
	 * @return new factory
	 */
	private static FixjureFactory newFactory(final ClassLoader clsLoader, final Fixture fixtureAnnotation) {
		assert fixtureAnnotation != null : "Annotation cannot be null.";
		assert clsLoader != null : "Classloader cannot be null.";

		return FixjureFactory.newFactory(
				  fixtureAnnotation.format().createSourceFactory(clsLoader, fixtureAnnotation.type())
		);
	}

	/** Util ctor. */
	private FixtureInjector() {
		// utility constructor
	}

	/**
	 * Searches for single-arg methods annotated with {@literal @Fixture}.
	 *
	 * @param obj object to scan
	 * @return set of methods
	 */
	private Iterable<AnnotatedMethodTarget> findAnnotatedMethods(final Object obj) {
		assert obj != null : "Object cannot be null.";

		final ImmutableSet.Builder<AnnotatedMethodTarget> builder = ImmutableSet.builder();
		for (final Method m : obj.getClass().getMethods()) {
         if (m.isAnnotationPresent(Fixture.class)) {
				// found one
				// want a signature with one argument, matching annotation type
				final Class<?>[] paramTypes = m.getParameterTypes();
				if (paramTypes.length == 1) {
					builder.add(new AnnotatedMethodTarget(m, m.getAnnotation(Fixture.class)));
				} else {
					throw new FixtureException("Methods annotated with @Fixture must accept only one argument.");
				}
			}
      }
		return builder.build();
	}

	private Iterable<AnnotatedMethodParameterTarget> findAnnotatedMethodParameters(final Object obj) {
		assert obj != null : "Object cannot be null.";

		final ImmutableSet.Builder<AnnotatedMethodParameterTarget> builder = ImmutableSet.builder();
		for (final Method m : obj.getClass().getMethods()) {
			if (!m.isAnnotationPresent(Fixture.class) && m.getParameterTypes().length > 0) {
				final LinkedHashMap<Fixture, Class<?>> annotations = Maps.newLinkedHashMap();
				if (Iterables.all(Arrays.asList(m.getParameterAnnotations()), new Predicate<Annotation[]>() {

					private int i = 0;

					@Override
					public boolean apply(@Nullable final Annotation[] o) {
						final int j = i++;
						return Iterables.any(Arrays.asList(o), new Predicate<Annotation>() {
							@Override
							public boolean apply(@Nullable final Annotation annotation) {
								if (annotation instanceof Fixture) {
									annotations.put((Fixture) annotation, m.getParameterTypes()[j]);
									return true;
								} else {
									return false;
								}
							}
						});
					}
				})) {
					builder.add(new AnnotatedMethodParameterTarget(m, annotations));
				}
			}
		}
		return builder.build();
	}
}
