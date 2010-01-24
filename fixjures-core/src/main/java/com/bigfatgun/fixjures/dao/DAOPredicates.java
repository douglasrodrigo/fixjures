package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.extract.Extractor;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;

public class DAOPredicates {
	private DAOPredicates() { /* util */ }

	public static <F> Predicate<F> page(final int pageSize, final int pageNumber) {
		return new Predicate<F>() {
			private final int start = pageSize * pageNumber;
			private final int end = start + pageSize;
			private int count = 0;
			@Override
			public boolean apply(@Nullable F f) {
				int current = count++; // zero-based
				return current >= start && current < end;
			}
		};
	}

	public static <F, T> Predicate<F> valueIsNull(final Extractor<F, T> extractor) {
		return Predicates.compose(Predicates.isNull(), extractor);
	}

	public static <F, T> Predicate<F> valueIsIn(final Extractor<F, T> extractor, Collection<? extends T> values) {
		return Predicates.compose(Predicates.in(values), extractor);
	}

	public static <F, T extends Comparable<? super T>> Predicate<F> valueIsGreaterThanOrEqualTo(
			final Extractor<F, T> extractor,
			final T value) {
		return new Predicate<F>() {
			@Override
			public boolean apply(@Nullable F o) {
				return extractor.apply(o).compareTo(value) >= 0;
			}
		};
	}

	public static <F, T> Predicate<F> valueEquals(final Extractor<F, T> extractor, final T value) {
		if (value == null) {
			return valueIsNull(extractor);
		} else {
			return Predicates.compose(Predicates.equalTo(value), extractor);
		}
	}
}
