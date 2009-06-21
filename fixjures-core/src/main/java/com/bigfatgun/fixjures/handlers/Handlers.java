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
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class Handlers {

	public static FixtureHandler<Byte> byteHandler() {
		return new NumberFixtureHandler<Byte>(Byte.class, Byte.TYPE) {
			@Override
			protected Byte narrowNumericValue(final Number number) {
				return number.byteValue();
			}
		};
	}

	public static FixtureHandler<Short> shortHandler() {
		return new NumberFixtureHandler<Short>(Short.class, Short.TYPE) {
			@Override
			protected Short narrowNumericValue(final Number number) {
				return number.shortValue();
			}
		};
	}

	public static FixtureHandler<Integer> integerHandler() {
		return new NumberFixtureHandler<Integer>(Integer.class, Integer.TYPE) {
			@Override
			protected Integer narrowNumericValue(final Number number) {
				return number.intValue();
			}
		};
	}

	public static FixtureHandler<Long> longHandler() {
		return new NumberFixtureHandler<Long>(Long.class, Long.TYPE) {
			@Override
			protected Long narrowNumericValue(final Number number) {
				return number.longValue();
			}
		};
	}

	public static FixtureHandler<Float> floatHandler() {
		return new NumberFixtureHandler<Float>(Float.class, Float.TYPE) {
			@Override
			protected Float narrowNumericValue(final Number number) {
				return number.floatValue();
			}
		};
	}

	public static FixtureHandler<Double> doubleHandler() {
		return new NumberFixtureHandler<Double>(Double.class, Double.TYPE) {
			@Override
			protected Double narrowNumericValue(final Number number) {
				return number.doubleValue();
			}
		};
	}

	public static FixtureHandler<StringBuilder> stringBuilderHandler() {
		return new StringBuilderFixtureHandler();
	}

	public static <InterimType, EndType> Iterable<FixtureHandler<? extends EndType>> createChain(
			  final ChainedFixtureHandler<InterimType> chained,
			  final ImmutableList<FixtureHandler<EndType>> handlers) {

		final List<FixtureHandler<? extends EndType>> fixtures = Lists.newLinkedList();
		for (final FixtureHandler<EndType> handler : handlers) {
			fixtures.add(chained.link(handler));
		}
		return Collections.unmodifiableList(fixtures);
	}

	public static FixtureHandler<BigInteger> bigIntegerHandler() {
		return new BigIntegerFixtureHandler();
	}

	public static FixtureHandler<BigDecimal> bigDecimalHandler() {
		return new BigDecimalFixtureHandler();
	}

	public static FixtureHandler<Date> javaDateHandler() {
		return new JavaDateHandler();
	}
}
