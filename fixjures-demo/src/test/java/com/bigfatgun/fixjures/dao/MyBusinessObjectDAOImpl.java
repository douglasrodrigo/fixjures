package com.bigfatgun.fixjures.dao;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.google.inject.internal.Lists;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

final class MyBusinessObjectDAOImpl extends AbstractDAO<MyBusinessObject> implements MyBusinessObjectDAO {

	private final Predicate<MyBusinessObject> positiveAccountBalance;
	private final Ordering<MyBusinessObject> ascendingId;
	private final Ordering<MyBusinessObject> ascendingAccountBalance;

	public MyBusinessObjectDAOImpl(final DAOHelper<MyBusinessObject> helper) {
		super(helper, new Function<MyBusinessObject, String>() {
			@Override
			public String apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getId();
			}
		});
		// set up predicates and comparators for use in implementations
		positiveAccountBalance = new Predicate<MyBusinessObject>() {
			@Override
			public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getAccountBalance() >= 0L;
			}
		};
		ascendingId = Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		ascendingAccountBalance = Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getAccountBalance().compareTo(o2.getAccountBalance());
			}
		});
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
		return Lists.newArrayList(getHelper().findAllWhere(new Predicate<MyBusinessObject>() {
			@Override
			public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getAccountBalance() >= minimumBalance;
			}
		}));
	}

	@Override
	public int countByAccountBalanceGreaterThan(long minimumBalance) {
		return findByAccountBalanceGreaterThan(minimumBalance).size();
	}

	@Override
	public List<MyBusinessObject> findAllOrderedByAccountBalance() {
		return getHelper().findAllOrderedAscending(ascendingAccountBalance);
	}

	@Override
	public List<MyBusinessObject> findByPositiveAccountBalanceOrderedByIdDescending() {
		return getHelper().findAllOrderedDescendingWhere(ascendingId, positiveAccountBalance);
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
		return Lists.newArrayList(getHelper().findAllWhere(new Predicate<MyBusinessObject>() {
			@Override
			public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
				// It is safe to use .equals(Object) or == on the proxies,
				// when loaded by ID they will be the same instance.
				return parent == myBusinessObject.getParent();
			}
		}));
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
