/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.Fixjure;
import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureFactory;
import com.bigfatgun.fixjures.Strategies;
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
        Strategies.SourceStrategy strategy = Strategies.newResourceStrategy(getClass().getClassLoader(), Strategies.newFormatStringStrategy("%2$s"));
        FixtureFactory fact = FixtureFactory.newFactory(YamlSourceFactory.newFactory(strategy));
        MyObject obj = fact.createFixture(MyObject.class, "MyObject.1.yaml");
        assertNotNull(obj);
        assertEquals("yaml", obj.getName());
        assertEquals("YAML", obj.getValue());
        assertEquals(((long) Integer.MAX_VALUE) + 1L, obj.getFavoriteNumber());
        assertEquals(ImmutableSet.of("Sir Yamls-a-lot", "Banana Yaml", "Yaml Ama Ding Dong"), obj.getNicknames());
    }

	@Test(expected = FixtureException.class)
	public void yamlFactoryUnknownIdentifier() {
        Strategies.SourceStrategy strategy = Strategies.newResourceStrategy(getClass().getClassLoader(), Strategies.DEFAULT_CLASSPATH_NAME_STRATEGY);
        FixtureFactory fact = FixtureFactory.newFactory(YamlSourceFactory.newFactory(strategy));
		fact.createFixture(MyObject.class, "foo");
	}
}
