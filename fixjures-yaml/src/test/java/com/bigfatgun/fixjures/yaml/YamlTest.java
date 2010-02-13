package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

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
}
