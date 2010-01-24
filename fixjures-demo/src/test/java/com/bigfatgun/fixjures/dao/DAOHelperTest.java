package com.bigfatgun.fixjures.dao;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DAOHelperTest {

	private MyBusinessObjectDAO dao;
	private MyBusinessObjectDAOImpl daoImpl;

	@Before
	public void setup() {
		/*
		 * These two lines set up the DAOHelper. Could just as easily use an external source strategy
		 * where each object has its own .json file in the classpath (for example).
		 */
		final Map<String, String> map = ImmutableMap.<String, String>builder()
				.put("1", "{ \"id\" : \"1\", \"accountBalance\" : 100000, \"url\" : \"http://www.google.com/\" }")
				.put("2", "{ \"id\" : \"2\", \"accountBalance\" : 50000, \"url\" : \"http://www.yahoo.com/\" }")
				.put("3", "{ \"id\" : \"3\", \"accountBalance\" : -1, \"url\" : \"http://www.bigfatgun.com/\", \"parent\" : \"1\" }")
				.build();
		final DAOHelper<MyBusinessObject> helper = DAOHelper.forClassFromJSON(MyBusinessObject.class, map);

		/*
		 * A package-private implementation of the DAO that delegates to the DAOHelper for its calls. Uses
		 * Predicates and Functions from google-collections to do result filtering/sorting.
		 */
		dao = daoImpl = new MyBusinessObjectDAOImpl(helper);
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
	public void getObjectsSortedAndFiltered() {
		List<MyBusinessObject> filtered = dao.findByPositiveAccountBalanceOrderedByIdDescending();
		// assert that all objects have a 0 or greater balance
		assertTrue(Iterables.all(filtered, new Predicate<MyBusinessObject>() {
			@Override
			public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getAccountBalance() >= 0L;
			}
		}));
		// assert that all objects are in order of descending id
		assertTrue(Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getId().compareTo(o2.getId());
			}
		}).reverse().isOrdered(filtered));

		filtered = dao.findByNegativeAccountBalanceOrderedByIdDescending();
		// assert that all objects have negative balance
		assertTrue(Iterables.all(filtered, new Predicate<MyBusinessObject>() {
			@Override
			public boolean apply(@Nullable MyBusinessObject myBusinessObject) {
				return myBusinessObject.getAccountBalance() < 0L;
			}
		}));
		// assert that all objects are in order of descending id
		assertTrue(Ordering.from(new Comparator<MyBusinessObject>() {
			@Override
			public int compare(MyBusinessObject o1, MyBusinessObject o2) {
				return o1.getId().compareTo(o2.getId());
			}
		}).reverse().isOrdered(filtered));
	}

	@Test
	public void bogusId() {
		assertNull(dao.find("this ain't gonna work"));
	}

	@Test
	public void simulateDelete() {
		dao.delete(daoImpl.createUnsavedDummy("1", 0L));
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

	@Test
	public void fullCrud() {
		assertEquals(3, dao.findAll().size());
		assertNull(dao.find("4"));

		dao.insert(daoImpl.createUnsavedDummy("4", Long.MAX_VALUE));

		assertEquals(4, dao.findAll().size());
		MyBusinessObject newObject = dao.find("4");
		assertEquals(Long.MAX_VALUE, newObject.getAccountBalance().longValue());

		dao.update(daoImpl.createUnsavedDummy("4", Long.MIN_VALUE));

		assertEquals(4, dao.findAll().size());
		MyBusinessObject updatedObject = dao.find("4");
		assertNotSame(newObject, updatedObject);
		assertEquals(Long.MIN_VALUE, updatedObject.getAccountBalance().longValue());

		dao.delete(updatedObject);
		assertEquals(3, dao.findAll().size());
	}

	@Test
	public void timeToCreateAndRetrieveAndSortOneHundredThousand() {
		long n1 = System.nanoTime();
		for (int i = 5; i < 100005; i++) {
			dao.insert(daoImpl.createUnsavedDummy(String.valueOf(i), 100L * i));
		}
		long n2 = System.nanoTime();
		System.out.format("Created objects in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(n2 - n1));
		List<MyBusinessObject> unsorted = dao.findAll();
		long n4 = System.nanoTime();
		System.out.format("Retrieved %d unsorted objects in %d ms.%n", unsorted.size(), TimeUnit.NANOSECONDS.toMillis(n4 - n2));
		List<MyBusinessObject> sorted = daoImpl.findAllOrderByHashCodeForFun();
		long n3 = System.nanoTime();
		System.out.format("Retrieved %d sorted objects in %d ms.%n", sorted.size(), TimeUnit.NANOSECONDS.toMillis(n3 - n4));
		List<MyBusinessObject> page5 = daoImpl.findPaged(100, 5);
		long n5 = System.nanoTime();
		System.out.format("Retrieved page 5 in %d ms.%n", TimeUnit.NANOSECONDS.toMillis(n5 - n4));		
	}

	@Test
	public void orderByHash() {
		for (int i = 5; i < 100; i++) {
			dao.insert(daoImpl.createUnsavedDummy(String.valueOf(i), 100L * i));
		}
		System.out.println(Iterables.transform(daoImpl.findAllOrderByHashCodeForFun(), new Function<MyBusinessObject, Object>() {
			@Override
			public Object apply(@Nullable MyBusinessObject myBusinessObject) {
				return String.format("%s (hash = %d)", myBusinessObject.getId(), myBusinessObject.hashCode());
			}
		}));
	}
}
