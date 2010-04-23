package com.bigfatgun.fixjures.dao;

import com.google.common.base.Predicate;

public class DAOPredicates {
	private DAOPredicates() { /* util */ }

	public static <F> Predicate<F> page(final int pageSize, final int pageNumber) {
		return new Predicate<F>() {
			private final int start = pageSize * pageNumber;
			private final int end = start + pageSize;
			private int count = 0;
			@Override
			public boolean apply(F f) {
				int current = count++; // zero-based
				return current >= start && current < end;
			}
		};
	}
}
