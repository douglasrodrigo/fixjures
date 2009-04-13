/*
 * Copyright (C) 2009 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bigfatgun.fixjures.handlers;

import java.math.BigInteger;
import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: steve
 * Date: Apr 11, 2009
 * Time: 9:15:49 PM
 * To change this template use File | Settings | File Templates.
 */
class BigIntegerFixtureHandler extends AbstractFixtureHandler<Number,BigInteger> {

	public Class<BigInteger> getReturnType() {
		return BigInteger.class;
	}

	public Class<Number> getSourceType() {
		return Number.class;
	}

	public BigInteger apply(@Nullable final Number number) {
		return BigInteger.valueOf(number.longValue());
	}
}
