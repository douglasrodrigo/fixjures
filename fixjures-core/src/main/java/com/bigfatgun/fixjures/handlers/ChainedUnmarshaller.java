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

import com.bigfatgun.fixjures.FixtureType;
import com.bigfatgun.fixjures.TypeWrapper;
import com.google.common.base.Supplier;

/** Chained handler that can join two fixture handlers that have a return and source type in common. */
public abstract class ChainedUnmarshaller<T> extends AbstractUnmarshaller<T> {

	public ChainedUnmarshaller(final Class<?> sourceType, final Class<T> interimType) {
		super(sourceType, interimType);
	}

	public final <T1> Unmarshaller<T1> link(final Unmarshaller<T1> handler) {
		final FixtureType interimTypeDef = TypeWrapper.wrap(getReturnType());
		return new AbstractUnmarshaller<T1>(getSourceType(), handler.getReturnType()) {
			public Supplier<? extends T1> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {
				return handler.unmarshall(helper, ChainedUnmarshaller.this.unmarshall(helper, source, interimTypeDef).get(), typeDef);
			}
		};
	}
}
