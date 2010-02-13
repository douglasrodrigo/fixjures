package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureFactory;
import com.bigfatgun.fixjures.annotations.NativeSourceType;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class YamlTest {

	private interface MyObject {
		String getName();
		String getValue();
		long getFavoriteNumber();
		Set<String> getNicknames();
	}

	@Test
	public void basicYamlTest() {
		MyObject obj = Fixjure.of(MyObject.class).from(YamlSource.newYamlResource("MyObject.1.yaml")).create();
		assertEquals("yaml", obj.getName());
		assertEquals("YAML", obj.getValue());
		assertEquals(((long) Integer.MAX_VALUE) + 1L, obj.getFavoriteNumber());
		assertEquals(ImmutableSet.of("Sir Yamls-a-lot", "Banana Yaml", "Yaml Ama Ding Dong"), obj.getNicknames());
	}

	@Test
	public void yamlFactory() {
		FixtureFactory fact = FixtureFactory.newFactory(YamlSourceFactory.newFactoryFromSourceType(getClass().getClassLoader(), NativeSourceType.Resource));
		MyObject obj = fact.createFixture(MyObject.class, "MyObject.1.yaml");
		assertNotNull(obj);
		assertEquals("yaml", obj.getName());
		assertEquals("YAML", obj.getValue());
		assertEquals(((long) Integer.MAX_VALUE) + 1L, obj.getFavoriteNumber());
		assertEquals(ImmutableSet.of("Sir Yamls-a-lot", "Banana Yaml", "Yaml Ama Ding Dong"), obj.getNicknames());
	}

	@Test(expected = FixtureException.class)
	public void yamlFactoryUnknownIdentifier() {
		FixtureFactory fact = FixtureFactory.newFactory(YamlSourceFactory.newFactoryFromSourceType(getClass().getClassLoader(), NativeSourceType.Resource));
		fact.createFixture(MyObject.class, "foo");
	}
}
