package com.bigfatgun.fixjures.mock;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class MockHelperTest {

	private static interface MyInterface {
		// nothing in this interface
	}

	@Test
	public void mockCreatesAMock() {
		assertNotNull(MockHelper.mock(MyInterface.class));
	}
}
