package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.extract.Extractor;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;
import com.google.inject.internal.Lists;

import java.util.List;

final class MyBusinessObjectDAOImpl extends AbstractDAO<MyBusinessObject> implements MyBusinessObjectDAO {

	private static final Extractor<MyBusinessObject, Long> EXTRACT_ACCOUNT_BALANCE;
	private static final Extractor<MyBusinessObject, MyBusinessObject> EXTRACT_PARENT;
	private static final Predicate<MyBusinessObject> POSITIVE_ACCOUNT_BALANCE;
	private static final Ordering<MyBusinessObject> ASCENDING_ID;
	private static final Ordering<MyBusinessObject> ASCENDING_ACCOUNT_BALANCE;
	private static final Ordering<Object> ASCENDING_HASH;
	private static final Extractor<MyBusinessObject,String> ID_FUNCTION;

	static {
		// set up predicates and comparators for use in implementations
		ID_FUNCTION = new Extractor<MyBusinessObject, String>() {{ execute(MyBusinessObject.class).getId(); }};
		
		EXTRACT_ACCOUNT_BALANCE = new Extractor<MyBusinessObject, Long>() {{ execute(MyBusinessObject.class).getAccountBalance(); }};

		EXTRACT_PARENT = new Extractor<MyBusinessObject, MyBusinessObject>() {{ execute(MyBusinessObject.class).getParent(); }};

		POSITIVE_ACCOUNT_BALANCE = DAOPredicates.valueIsGreaterThanOrEqualTo(EXTRACT_ACCOUNT_BALANCE, 0L);

		ASCENDING_ID = Ordering.natural().onResultOf(ID_FUNCTION);
		ASCENDING_HASH = Ordering.natural().onResultOf(Extractor.ofHashCode());
		ASCENDING_ACCOUNT_BALANCE = Ordering.natural().onResultOf(EXTRACT_ACCOUNT_BALANCE);
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
		return Lists.newArrayList(getHelper().findAllWhere(
				DAOPredicates.valueIsGreaterThanOrEqualTo(EXTRACT_ACCOUNT_BALANCE, minimumBalance)
		));
	}

	@Override
	public int countByAccountBalanceGreaterThan(long minimumBalance) {
		return findByAccountBalanceGreaterThan(minimumBalance).size();
	}

	@Override
	public List<MyBusinessObject> findAllOrderedByAccountBalance() {
		return getHelper().findAllOrdered(ASCENDING_ACCOUNT_BALANCE);
	}

	@Override
	public List<MyBusinessObject> findByPositiveAccountBalanceOrderedByIdDescending() {
		return getHelper().findAllOrderedWhere(ASCENDING_ID.reverse(), POSITIVE_ACCOUNT_BALANCE);
	}

	@Override
	public List<MyBusinessObject> findByNegativeAccountBalanceOrderedByIdDescending() {
		return getHelper().findAllOrderedWhere(ASCENDING_ID.reverse(), Predicates.not(POSITIVE_ACCOUNT_BALANCE));
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
		return Lists.newArrayList(getHelper().findAllWhere(
				DAOPredicates.valueEquals(EXTRACT_PARENT, parent)
		));
	}

	List<MyBusinessObject> findAllOrderByHashCodeForFun() {
		return getHelper().findAllOrdered(ASCENDING_HASH);
	}

	List<MyBusinessObject> findPaged(int pageSize, int pageNumber) {
		return getHelper().findAllOrderedWhere(ASCENDING_ACCOUNT_BALANCE, DAOPredicates.page(pageSize, pageNumber), false);
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
