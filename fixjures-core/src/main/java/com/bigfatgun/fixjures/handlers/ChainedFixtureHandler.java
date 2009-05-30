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

/**
 * Chained handler that can join two fixture handlers that have a return and source type
 * in common.
 *
 * @param <S> source type
 * @param <I> interim type
 * @author Steve Reed
 */
public abstract class ChainedFixtureHandler<S,I> extends AbstractFixtureHandler<S,I> {

	/**
	 * Links this handler to the given one.
	 *
	 * @param handler handler to join
	 * @param <O> output type
	 * @return chained handler
	 */
	public final <O> FixtureHandler<S,O> link(final FixtureHandler<I,O> handler) {
		return new AbstractFixtureHandler<S, O>() {

			public Class<O> getReturnType() {
				return handler.getReturnType();
			}

			public Class<S> getSourceType() {
				return ChainedFixtureHandler.this.getSourceType();
			}

			public O apply(final HandlerHelper helper, final S s) {
				return handler.apply(helper, ChainedFixtureHandler.this.apply(helper, s));
			}
		};
	}
}
