package com.bigfatgun.fixjures.dao;

import java.util.List;

interface MyBusinessObjectDAO {

	MyBusinessObject find(String id);
	List<MyBusinessObject> findAll();

	// filtering
	List<MyBusinessObject> findByAccountBalanceGreaterThan(long minimumBalance);
	int countByAccountBalanceGreaterThan(long minimumBalance);

	// ordering
	List<MyBusinessObject> findAllOrderedByAccountBalance();

	// filtering AND ordering?!@#$
	List<MyBusinessObject> findByPositiveAccountBalanceOrderedByIdDescending();

	// deletes and inserts can be simulated
	void delete(MyBusinessObject obj);
	void insert(MyBusinessObject obj);

	// updates too!
	void update(MyBusinessObject obj);

	// associations are supported, just use the referenced object's ID in the source data
	List<MyBusinessObject> findChildren(MyBusinessObject parent);
}
