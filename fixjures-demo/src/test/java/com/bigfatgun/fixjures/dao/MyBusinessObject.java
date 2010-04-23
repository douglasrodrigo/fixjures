package com.bigfatgun.fixjures.dao;

import java.util.Collection;
import java.util.Set;

public interface MyBusinessObject {

	String getId();
	Long getAccountBalance();
	MyBusinessObject getParent();
    Collection<String> getSomeStrings();
    Set<Integer> getUniqueNumbers();
	
}
