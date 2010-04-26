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

import com.google.common.base.Function;

public abstract class AbstractDAO<T> {

	private final DAOHelper<T> helper;
	private final Function<? super T, String> idFunction;

	protected AbstractDAO(final DAOHelper<T> helper, final Function<? super T, String> idFunction) {
		this.helper = helper;
		this.idFunction = idFunction;
	}

	protected final DAOHelper<T> getHelper() {
		return this.helper;
	}

	protected final String getId(final T obj) {
		return idFunction.apply(obj);
	}

	protected final T doInsert(final T obj) {
		return this.helper.add(obj, getId(obj));
	}

	protected final T doSelect(final String id) {
		return this.helper.findById(id);
	}

	protected final T doUpdate(final T obj) {
		T prior = doDelete(obj);
		doInsert(obj);
		return prior;
	}

	protected final T doDelete(final T obj) {
		return this.helper.remove(getId(obj));
	}
}
