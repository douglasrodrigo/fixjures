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

import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import sun.font.LayoutPathImpl;

/**
 * Created by IntelliJ IDEA.
 * User: steve
 * Date: Apr 5, 2009
 * Time: 9:10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Handlers {

	public static FixtureHandler<Number,Byte> byteHandler() {
		return new ByteFixtureHandler();
	}

	public static FixtureHandler<Number,Short> shortHandler() {
		return new ShortFixtureHandler();
	}

	public static FixtureHandler<Number,Integer> integerHandler() {
		return new IntegerFixtureHandler();
	}

	public static FixtureHandler<Number,Long> longHandler() {
		return new LongFixtureHandler();
	}

	public static FixtureHandler<Number,Float> floatHandler() {
		return new FloatFixtureHandler();
	}

	public static FixtureHandler<Number,Double> doubleHandler() {
		return new DoubleFixtureHandler();
	}

	public static FixtureHandler<CharSequence,StringBuilder> stringBuilderHandler() {
		return new StringBuilderFixtureHandler();
	}

	public static <SourceType, InterimType, EndType> Iterable<FixtureHandler<JSONSource.SourceType,? extends LayoutPathImpl.EndType>> createChain(
			  final ChainedFixtureHandler<JSONSource.SourceType, InterimType> chained,
			  final ImmutableList<FixtureHandler<InterimType, LayoutPathImpl.EndType>> handlers) {

		final List<FixtureHandler<JSONSource.SourceType,? extends LayoutPathImpl.EndType>> fixtures = Lists.newLinkedList();
		for (final FixtureHandler<InterimType, LayoutPathImpl.EndType> handler : handlers) {
			fixtures.add(chained.link(handler));
		}
		return Collections.unmodifiableList(fixtures);
	}

	public static FixtureHandler<Number, BigInteger> bigIntegerHandler() {
		return new BigIntegerFixtureHandler();
	}

	public static FixtureHandler<Number, BigDecimal> bigDecimalHandler() {
		return new BigDecimalFixtureHandler();
	}

	public static FixtureHandler<CharSequence, Date> javaDateHandler() {
		return new JavaDateHandler();
	}
}
