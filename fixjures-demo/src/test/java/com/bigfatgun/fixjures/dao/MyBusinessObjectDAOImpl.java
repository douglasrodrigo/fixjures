package com.bigfatgun.fixjures.dao;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;
import com.google.inject.internal.Lists;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

final class MyBusinessObjectDAOImpl extends AbstractDAO<MyBusinessObject> implements MyBusinessObjectDAO {

	private static final Predicate<MyBusinessObject> POSITIVE_ACCOUNT_BALANCE;
	private static final Ordering<MyBusinessObject> ASCENDING_ID;
	private static final Ordering<MyBusinessObject> ASCENDING_ACCOUNT_BALANCE;
	private static final Function<MyBusinessObject,String> ID_FUNCTION;

	static {
		// set up predicates and comparators for use in implementations
		POSITIVE_ACCOUNT_BALANCE = DAOPredicates.methodValueIsGreaterThanOrEqualTo(MyBusinessObject.class, "getAccountBalance", Long.class, 0L);
		ASCENDING_ID = Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		ASCENDING_ACCOUNT_BALANCE = Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getAccountBalance().compareTo(o2.getAccountBalance());
			}
		});
		ID_FUNCTION = new Function<MyBusinessObject, String>() {
			@Override
			public String apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getId();
			}
		};
	}

	public MyBusinessObjectDAOImpl(final DAOHelper<MyBusinessObject> helper) {
		super(helper, ID_FUNCTION);
	}

	@Override
	public MyBusinessObject find(String id) {
		return doSelect(id);
	}

	@Override
	public List<MyBusinessObject> findAll() {
		return Lists.newArrayList(getHelper().findAll());
	}

	@Override
	public List<MyBusinessObject> findByAccountBalanceGreaterThan(final long minimumBalance) {
		return Lists.newArrayList(getHelper().findAllWhereAll(DAOPredicates.methodValueIsGreaterThanOrEqualTo(MyBusinessObject.class, "getAccountBalance", Long.class, minimumBalance)));
	}

	@Override
	public int countByAccountBalanceGreaterThan(long minimumBalance) {
		return findByAccountBalanceGreaterThan(minimumBalance).size();
	}

	@Override
	public List<MyBusinessObject> findAllOrderedByAccountBalance() {
		return getHelper().findAllOrderedAscending(ASCENDING_ACCOUNT_BALANCE);
	}

	@Override
	public List<MyBusinessObject> findByPositiveAccountBalanceOrderedByIdDescending() {
		return getHelper().findAllOrderedDescendingWhere(ASCENDING_ID, POSITIVE_ACCOUNT_BALANCE);
	}

	@Override
	public void delete(MyBusinessObject obj) {
		doDelete(obj);
	}

	@Override
	public void insert(MyBusinessObject obj) {
		MyBusinessObject prior = doInsert(obj);
		if (prior != null) {
			System.err.format("WARNING: Insert of new object overwrote prior value: %s%n", prior);
		}
	}

	@Override
	public void update(MyBusinessObject obj) {
		doUpdate(obj);
	}

	@Override
	public List<MyBusinessObject> findChildren(final MyBusinessObject parent) {
		return Lists.newArrayList(getHelper().findAllWhereAll(
				DAOPredicates.methodValueMatches(MyBusinessObject.class, "getParent", MyBusinessObject.class, parent)
		));
	}

	MyBusinessObject createUnsavedDummy(final String id, final Long accountBalance) {
		// Do whatever you want here. If you have an impl that you want to use, or if you're
		// using something like jmock to create stubs, do that here.
		return new MyBusinessObject() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public Long getAccountBalance() {
				return accountBalance;
			}

			@Override
			public MyBusinessObject getParent() {
				return null;
			}
		};
	}
}
