package com.bigfatgun.fixjures;

import org.junit.Test;


public class FixjureFactoryTest {

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceFactory() {
		FixtureFactory.newFactory(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPointerExceptionWithNullSourceStrategyForObjInStreamFactory() {
		FixtureFactory.newObjectInputStreamFactory(null);
	}
}
