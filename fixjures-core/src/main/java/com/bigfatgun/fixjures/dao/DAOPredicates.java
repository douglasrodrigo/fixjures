/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
