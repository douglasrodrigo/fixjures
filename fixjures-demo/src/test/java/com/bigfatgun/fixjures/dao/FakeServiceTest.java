package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.IdentifierProvider;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.yaml.YamlSourceFactory;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FakeServiceTest {

	private FakeService svc;
	private MyBusinessObjectDAO dao;

	@Before
	public void setup() {
		final Strategies.SourceStrategy strategy = Strategies.newClasspathStrategy(new Strategies.ResourceNameStrategy() {
			@Override
			public String getResourceName(Class<?> type, String name) {
				return String.format("com/bigfatgun/fixjures/dao/%s.%s.yaml", type.getSimpleName(), name);
			}
		});
		final DAOHelper<MyBusinessObject> helper = DAOHelper.forClass(MyBusinessObject.class, YamlSourceFactory.newFactory(strategy), new IdentifierProvider() {
			@Override
			public Iterable<String> existingObjectIdentifiers() {
				return ImmutableList.of("1", "2", "3", "4", "5");
			}
		});

		/*
		 * A package-private implementation of the DAO that delegates to the DAOHelper for its calls. Uses
		 * Predicates and Functions from google-collections to do result filtering/sorting.
		 */
		dao = new MyBusinessObjectDAOImpl(helper);
		svc = new FakeService(dao);
	}

	@Test
	public void lowestBalanceDeleteWorks() {
		assertEquals(5, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(4, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(3, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(2, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(1, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(0, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(0, dao.findAll().size());
		svc.deleteObjectWithLowestBalance();
		assertEquals(0, dao.findAll().size());
	}
}
