package com.bigfatgun.fixjures.dao;

import java.util.List;

interface MyBusinessObjectDAO {

	// some finder methods here
	MyBusinessObject find(String id);
	List<MyBusinessObject> findAll();

	// methods that require filtering
	List<MyBusinessObject> findByAccountBalanceGreaterThan(long minimumBalance);
	int countByAccountBalanceGreaterThan(long minimumBalance);

	// methods that require sort
	List<MyBusinessObject> findAllOrderedByAccountBalance();

	// filtering AND ordering?!@#$
	List<MyBusinessObject> findByPositiveAccountBalanceOrderedByIdDescending();
	List<MyBusinessObject> findByNegativeAccountBalanceOrderedByIdDescending();

	// deletes and inserts can be simulated
	void delete(MyBusinessObject obj);
	void insert(MyBusinessObject obj);

	// updates too!
	void update(MyBusinessObject obj);

	// associations are supported, just use the referenced object's ID in the source data
	List<MyBusinessObject> findChildren(MyBusinessObject parent);
}
