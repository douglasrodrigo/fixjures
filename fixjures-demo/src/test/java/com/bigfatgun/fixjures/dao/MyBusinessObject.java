package com.bigfatgun.fixjures.dao;

interface MyBusinessObject {
	String getId();
	Long getAccountBalance();
	MyBusinessObject getParent();
}
