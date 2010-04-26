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

package com.bigfatgun.fixjures.handlers;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class NumberFixtureHandlerTest {

	private NumberUnmarshaller handler;

	@Test
	public void theTypeIsRight() {
		handler = new NumberUnmarshaller<Number>(Number.class, Number.class) {
			@Override
			protected Number narrowNumericValue(final Number number) {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}
		};
		assertSame(Number.class, handler.getSourceType());
	}

    @Test
    public void dateUnmarshallerTest() {
        assertEquals(213951600000L, new DateUnmarshaller().unmarshall(null, "1976-10-12", null).get().getTime());
    }
}
