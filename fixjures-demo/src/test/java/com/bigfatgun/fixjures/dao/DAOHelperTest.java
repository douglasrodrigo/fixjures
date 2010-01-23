package com.bigfatgun.fixjures.dao;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.internal.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DAOHelperTest {

	private interface MyBusinessObject {
		String getId();
		Long getAccountBalance();
		MyBusinessObject getParent();
	}

	private interface MyBusinessObjectDAO {

		MyBusinessObject find(String id);
		List<MyBusinessObject> findAll();

		// filtering
		List<MyBusinessObject> findByAccountBalanceGreaterThan(long minimumBalance);
		int countByAccountBalanceGreaterThan(long minimumBalance);

		// ordering
		List<MyBusinessObject> findAllOrderedByAccountBalance();

		// deletes and inserts can be simulated
		boolean delete(MyBusinessObject obj);

		// associations are supported, just use the references objects ID in the source data
		List<MyBusinessObject> findChildren(MyBusinessObject parent);
	}

	private MyBusinessObjectDAO dao;

	@Before
	public void setup() {
		/*
		 * These two lines set up the DAOHelper. Could just as easily use an external source strategy
		 * where each object has its own .json file in the classpath (for example).
		 *
		 * The DAOHelper should be seeded with data for every POSSIBLE object that will exist during the test.
		 * Then, to simulate deletes, just tell the helper which ids to return in its results. To simulate an
		 * insert you would initially seed the helper with data for the object, but ensure that its id
		 * was removed. Then.. just add the id:
		 *
		 * DAOHelper<T> helper = DAOHelper.for...;
		 * helper.removeIdentifier(idToBeInsertedLater);
		 *
		 * helper.findAll(); // excludes the record that will be inserted
		 * helper.addIdentifier(idToBeInsertedLater);
		 * helper.findAll(); // "new" record now appears
		 */
		final Map<String, String> map = ImmutableMap.<String, String>builder()
				.put("1", "{ \"id\" : \"1\", \"accountBalance\" : 100000, \"url\" : \"http://www.google.com/\" }")
				.put("2", "{ \"id\" : \"2\", \"accountBalance\" : 50000, \"url\" : \"http://www.yahoo.com/\" }")
				.put("3", "{ \"id\" : \"3\", \"accountBalance\" : -1, \"url\" : \"http://www.bigfatgun.com/\", \"parent\" : \"1\" }")
				.build();
		final DAOHelper<MyBusinessObject> helper = DAOHelper.forClassFromJSON(MyBusinessObject.class, map);

		/*
		 * An anonymous implementation of the DAO that delegates to the DAOHelper for its calls. Uses
		 * Predicates and Functions from google-collections to do result filtering/sorting.
		 */
		dao = new MyBusinessObjectDAO() {

			@Override
			public MyBusinessObject find(String id) {
				return helper.findById(id);
			}

			@Override
			public List<MyBusinessObject> findAll() {
				return Lists.newArrayList(helper.findAll());
			}

			@Override
			public List<MyBusinessObject> findByAccountBalanceGreaterThan(final long minimumBalance) {
				return Lists.newArrayList(helper.findAllWhere(new Predicate<MyBusinessObject>() {
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
				return helper.findAllOrdered(new Comparator<MyBusinessObject>() {
					@Override
					public int compare(MyBusinessObject o1, MyBusinessObject o2) {
						return o1.getAccountBalance().compareTo(o2.getAccountBalance());
					}
				});
			}

			@Override
			public boolean delete(MyBusinessObject obj) {
				return helper.removeIdentifier(obj.getId());
			}

			@Override
			public List<MyBusinessObject> findChildren(final MyBusinessObject parent) {
				return Lists.newArrayList(helper.findAllWhere(new Predicate<MyBusinessObject>() {
					@Override
					public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
						// It is safe to use .equals(Object) or == on the proxies,
						// when loaded by ID they will be the same instance.
						return parent == myBusinessObject.getParent();
					}
				}));
			}
		};
	}

	@Test
	public void getObjectsSortedByAccountBalance() {
		List<MyBusinessObject> sorted = dao.findAllOrderedByAccountBalance();
		assertEquals("3", sorted.get(0).getId());
		assertEquals("2", sorted.get(1).getId());
		assertEquals("1", sorted.get(2).getId());
	}

	@Test
	public void getObjectsFilteredByAccountBalance() {
		List<MyBusinessObject> filtered = dao.findByAccountBalanceGreaterThan(50001L);
		assertEquals(1, filtered.size());
		assertEquals("1", Iterables.getOnlyElement(filtered).getId());
		assertEquals(1, dao.countByAccountBalanceGreaterThan(50001L));
		assertEquals(2, dao.countByAccountBalanceGreaterThan(50000L));
		assertEquals(3, dao.countByAccountBalanceGreaterThan(-50000L));
	}

	@Test
	public void bogusId() {
		assertNull(dao.find("this ain't gonna work"));
	}

	@Test
	public void simulateDelete() {
		dao.delete(new MyBusinessObject() {
			@Override
			public String getId() {
				return "1";
			}

			@Override
			public Long getAccountBalance() {
				return 0L;
			}

			@Override
			public MyBusinessObject getParent() {
				return null;
			}
		});
		assertEquals(2, dao.findAll().size());
		assertNull(dao.find("1"));
	}

	@Test
	public void findByAssociation() {
		MyBusinessObject expectedChild = dao.find("3");
		MyBusinessObject parent = dao.find("1");
		MyBusinessObject child = Iterables.getOnlyElement(dao.findChildren(parent));
		assertEquals(expectedChild.getId(), child.getId());

		// DAOHelper caches references by id, and should return the same instance.
		// these proxies are immutable, so this practice is safe and efficient.
		assertSame(expectedChild, child);
	}
}
