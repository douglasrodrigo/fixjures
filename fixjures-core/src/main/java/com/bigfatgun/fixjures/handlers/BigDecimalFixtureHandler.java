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

import java.math.BigDecimal;

import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;
import com.bigfatgun.fixjures.FixtureTypeDefinition;

class BigDecimalFixtureHandler extends AbstractFixtureHandler<BigDecimal> {

	public BigDecimalFixtureHandler() {
		super(Number.class, BigDecimal.class);
	}

	@Override
	public ValueProvider<BigDecimal> apply(final HandlerHelper helper, final FixtureTypeDefinition typeDef, final Object source) {
		return ValueProviders.of(BigDecimal.valueOf(castSourceValue(Number.class, source).doubleValue()));
	}
}
