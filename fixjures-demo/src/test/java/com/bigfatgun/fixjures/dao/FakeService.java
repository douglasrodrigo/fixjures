package com.bigfatgun.fixjures.dao;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.List;
import java.util.Map;

public class FakeService {

	private MyBusinessObjectDAO dao;

	public FakeService(MyBusinessObjectDAO dao) {
		this.dao = dao;
	}

	public int calculateAccountsWithHigherBalance(MyBusinessObject obj) {
		return dao.countByAccountBalanceGreaterThan(obj.getAccountBalance());
	}

	public void deleteObjectWithLowestBalance() {
		List<MyBusinessObject> businessObjectList = dao.findAllOrderedByAccountBalance();
		if (!Iterables.isEmpty(businessObjectList)) {
			dao.delete(businessObjectList.iterator().next());
		}
	}
}
