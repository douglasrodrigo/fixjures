package com.bigfatgun.fixjures;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FixjureTest {

	@Test
	public void constructorIsPrivate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
		Constructor<Fixjure> ctor = Fixjure.class.getDeclaredConstructor();
		assertFalse(ctor.isAccessible());
		ctor.setAccessible(true);
		assertNotNull(ctor.newInstance());
	}
}
