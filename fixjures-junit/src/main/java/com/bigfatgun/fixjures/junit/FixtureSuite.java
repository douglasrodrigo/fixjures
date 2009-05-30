package com.bigfatgun.fixjures.junit;

import com.bigfatgun.fixjures.FixtureInjector;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class FixtureSuite extends BlockJUnit4ClassRunner {

	public FixtureSuite(final Class testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected Object createTest() throws Exception {
		final Object obj = super.createTest();
		FixtureInjector.scan(obj);
		return obj;
	}
}
