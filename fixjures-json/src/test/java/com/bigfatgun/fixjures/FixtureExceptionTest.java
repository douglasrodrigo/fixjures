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

package com.bigfatgun.fixjures;

import static org.junit.Assert.*;
import org.junit.Test;

@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class FixtureExceptionTest {

	@Test
	public void testMessageCtor() {
		assertEquals("the message", new FixtureException("the message").getMessage());
		assertNull(new FixtureException("the message").getCause());
	}

	@Test
	public void testCauseCtor() {
		assertEquals("java.lang.RuntimeException: the message", FixtureException.convert(new RuntimeException("the message")).getMessage());
		assertNotNull(FixtureException.convert(new RuntimeException()).getCause());
	}

	@Test
	public void convertFixtureException() {
		final FixtureException expected = new FixtureException("msg");
		assertSame(expected, FixtureException.convert(expected));
	}

	@Test
	public void convertNull() {
		assertNotNull(FixtureException.convert(null));
	}

	@Test
	public void convertRte() {
		final RuntimeException expected = new RuntimeException();
		assertSame(expected, FixtureException.convert(expected).getCause());
	}
}
