package com.bigfatgun.fixjures.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GuiceFixtureSuite extends BlockJUnit4ClassRunner {

	private static final Injector INJ = Guice.createInjector(new FixtureModule());

	public GuiceFixtureSuite(Class<?> aClass) throws InitializationError {
		super(aClass);
	}

	@Override
	protected Object createTest() throws Exception {
		try {
			return INJ.getInstance(getTestClass().getJavaClass());
		} catch (Exception e) {
			final Object obj = super.createTest();
			INJ.injectMembers(obj);
			return obj;
		}
	}
}
