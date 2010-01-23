package com.bigfatgun.fixjures.dao;

import com.google.common.base.Function;

public abstract class AbstractDAO<T> {

	private final DAOHelper<T> helper;
	private final Function<T, String> idFunction;

	protected AbstractDAO(final DAOHelper<T> helper, final Function<T, String> idFunction) {
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
