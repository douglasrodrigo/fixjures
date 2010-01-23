package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class DAOPredicates {
	private DAOPredicates() { /* util */ }

	public static <T> Predicate<T> methodValueIsNull(final Class<T> cls, final String methodName) {
		return methodValueMatches(cls, methodName, Object.class, null);
	}

	public static <T, F extends Comparable<? super F>> Predicate<T> methodValueIsGreaterThanOrEqualTo(final Class<T> cls, final String methodName, final Class<F> fieldCls, final F value) {
		return composeFieldPredicate(cls, methodName, fieldCls, new Predicate<F>() {
			@Override
			public boolean apply(@Nullable F obj) {
				return obj.compareTo(value) >= 0;
			}
		});
	}

	public static <T, F> Predicate<T> methodValueMatches(final Class<T> cls, final String methodName, final Class<F> fieldCls, final F value) {
		return composeFieldPredicate(cls, methodName, fieldCls, Predicates.equalTo(value));
	}

	public static <T, F> Predicate<T> composeFieldPredicate(final Class<T> cls, final String methodName, final Class<F> fieldCls, final Predicate<F> fieldPredicate) {
		try {
			Function<Object, F> cast = new ValueCastFunction<F>(fieldCls);
			Function<T, Object> value = new MethodValueFunction<T>(cls, methodName);
			Function<T, ? extends F> function = Functions.compose(cast, value);

			return Predicates.compose(fieldPredicate, function);
		} catch (Exception e) {
			return FixtureException.convertAndThrowAs(e);
		}
	}

	private static final class ValueCastFunction<T> implements Function<Object, T> {

		private final Class<T> cls;

		public ValueCastFunction(final Class<T> cls) {
			this.cls = cls;
		}

		@Override
		public T apply(@Nullable Object o) {
			return cls.cast(o);
		}
	}

	private static final class MethodValueFunction<T> implements Function<T, Object> {

		private final Method method;

		public MethodValueFunction(final Class<?> cls, final String fieldName) throws NoSuchMethodException {
			this.method = cls.getMethod(fieldName);
		}

		@Override
		public Object apply(@Nullable T o) {
			try {
				return method.invoke(o);
			} catch (Exception e) {
				return FixtureException.convertAndThrowAs(e);
			}
		}
	}
}
