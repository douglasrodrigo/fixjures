/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
