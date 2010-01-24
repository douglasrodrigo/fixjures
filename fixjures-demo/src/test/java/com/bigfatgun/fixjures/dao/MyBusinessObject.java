package com.bigfatgun.fixjures.dao;

public interface MyBusinessObject {
	String getId();
	Long getAccountBalance();
	MyBusinessObject getParent();
}
