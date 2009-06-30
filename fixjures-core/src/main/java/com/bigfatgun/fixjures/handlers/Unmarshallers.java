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

public final class Unmarshallers {

	public static Unmarshaller<Byte> byteHandler() {
		return new NumberUnmarshaller<Byte>(Byte.class, Byte.TYPE) {
			@Override
			protected Byte narrowNumericValue(final Number number) {
				return number.byteValue();
			}
		};
	}

	public static Unmarshaller<Short> shortHandler() {
		return new NumberUnmarshaller<Short>(Short.class, Short.TYPE) {
			@Override
			protected Short narrowNumericValue(final Number number) {
				return number.shortValue();
			}
		};
	}

	public static Unmarshaller<Integer> integerHandler() {
		return new NumberUnmarshaller<Integer>(Integer.class, Integer.TYPE) {
			@Override
			protected Integer narrowNumericValue(final Number number) {
				return number.intValue();
			}
		};
	}

	public static Unmarshaller<Long> longHandler() {
		return new NumberUnmarshaller<Long>(Long.class, Long.TYPE) {
			@Override
			protected Long narrowNumericValue(final Number number) {
				return number.longValue();
			}
		};
	}

	public static Unmarshaller<Float> floatHandler() {
		return new NumberUnmarshaller<Float>(Float.class, Float.TYPE) {
			@Override
			protected Float narrowNumericValue(final Number number) {
				return number.floatValue();
			}
		};
	}

	public static Unmarshaller<Double> doubleHandler() {
		return new NumberUnmarshaller<Double>(Double.class, Double.TYPE) {
			@Override
			protected Double narrowNumericValue(final Number number) {
				return number.doubleValue();
			}
		};
	}

	public static Unmarshaller<StringBuilder> stringBuilderHandler() {
		return new StringBuilderUnmarshaller();
	}

	public static <InterimType, EndType> Iterable<Unmarshaller<? extends EndType>> createChain(
			  final ChainedUnmarshaller<InterimType> chained,
			  final ImmutableList<Unmarshaller<EndType>> handlers) {

		final List<Unmarshaller<? extends EndType>> fixtures = Lists.newLinkedList();
		for (final Unmarshaller<EndType> handler : handlers) {
			fixtures.add(chained.link(handler));
		}
		return Collections.unmodifiableList(fixtures);
	}

	public static Unmarshaller<BigInteger> bigIntegerHandler() {
		return new BigIntegerUnmarshaller();
	}

	public static Unmarshaller<BigDecimal> bigDecimalHandler() {
		return new BigDecimalUnmarshaller();
	}

	public static Unmarshaller<Date> javaDateHandler() {
		return new DateUnmarshaller();
	}
}
