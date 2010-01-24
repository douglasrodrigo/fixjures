package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.extract.Extractor;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class DAOPredicates {
	private DAOPredicates() { /* util */ }

	public static <T> Predicate<T> extractedValueIsNull(final Extractor<?> extractor) {
		return new Predicate<T>() {
			@Override
			public boolean apply(@Nullable T t) {
				return extractor.apply(t) == null;
			}
		};
	}

	public static <F extends Comparable<? super F>> Predicate<Object> extractedValueIsGreaterThanOrEqualTo(
			final Extractor<F> extractor,
			final F value) {
		return new Predicate<Object>() {
			@Override
			public boolean apply(@Nullable Object o) {
				return extractor.apply(o).compareTo(value) >= 0;
			}
		};
	}

	public static Predicate<Object> extractedValueEquals(final Extractor<?> extractor, final Object value) {
		if (value == null) {
			return extractedValueIsNull(extractor);
		}

		return new Predicate<Object>() {
			@Override
			public boolean apply(@Nullable Object o) {
				return value.equals(extractor.apply(o));
			}
		};
	}
}
