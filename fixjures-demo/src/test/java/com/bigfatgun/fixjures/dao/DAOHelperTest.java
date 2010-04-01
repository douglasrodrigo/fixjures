package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.IdentifierProvider;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.yaml.YamlSource;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DAOHelperTest {

	private MyBusinessObjectDAO dao;
	private MyBusinessObjectDAOImpl daoImpl;
	private MyBusinessObjectDAOImpl daoImpl2;

	@Before
	public void setup() {
		/*
		 * This next chunk sets up the DAOHelper. Could just as easily use an external source strategy
		 * where each object has its own .json file in the classpath (for example).
		 */
//		final Map<String, String> map = ImmutableMap.<String, String>builder()
//				.put("1", "{ \"id\" : \"1\", \"accountBalance\" : 100000, \"url\" : \"http://www.google.com/\" }")
//				.put("2", "{ \"id\" : \"2\", \"accountBalance\" : 50000, \"url\" : \"http://www.yahoo.com/\" }")
//				.put("3", "{ \"id\" : \"3\", \"accountBalance\" : -1, \"url\" : \"http://www.bigfatgun.com/\", \"parent\" : \"1\" }")
//				.build();
//		final DAOHelper<MyBusinessObject> helper = DAOHelper.forClassFromJSON(MyBusinessObject.class, map);
		final Strategies.SourceStrategy strategy = Strategies.newClasspathStrategy(new Strategies.ResourceNameStrategy() {
			@Override
			public String getResourceName(Class<?> type, String name) {
				return String.format("com/bigfatgun/fixjures/dao/%s.%s.json", type.getSimpleName(), name);
			}
		});
		final DAOHelper<MyBusinessObject> helper = DAOHelper.forClassFromJSON(MyBusinessObject.class, strategy, new IdentifierProvider() {
			@Override
			public Iterable<String> existingObjectIdentifiers() {
				return ImmutableList.of("1", "2", "3");
			}
		});

		/*
		 * A package-private implementation of the DAO that delegates to the DAOHelper for its calls. Uses
		 * Predicates and Functions from google-collections to do result filtering/sorting.
		 */
		dao = daoImpl = new MyBusinessObjectDAOImpl(helper);
        daoImpl2 = new MyBusinessObjectDAOImpl(DAOHelper.forClassFromSingleSource(MyBusinessObject.class, YamlSource.newYamlResource("com/bigfatgun/fixjures/dao/mbo.yaml"), new Function<MyBusinessObject, String>() {
            @Override
            public String apply(@Nullable MyBusinessObject myBusinessObject) {
                return myBusinessObject.getId();
            }
        }));
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
		System.out.format("Retrieved %d items in page 5 in %d ms.%n", page5.size(), TimeUnit.NANOSECONDS.toMillis(n5 - n4));		
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

    @Test
    public void singleFileBasedDAO() {
        MyBusinessObject mbo1 = daoImpl2.find("1");
        MyBusinessObject mbo2 = daoImpl2.find("2");
        MyBusinessObject mbo3 = daoImpl2.find("3");
        MyBusinessObject mbo4 = daoImpl2.find("4");
        MyBusinessObject mbo5 = daoImpl2.find("5");
        MyBusinessObject mbo6 = mbo5.getParent();
        assertNotNull(mbo6);
        assertEquals(106, mbo6.getAccountBalance().longValue());
    }
}
