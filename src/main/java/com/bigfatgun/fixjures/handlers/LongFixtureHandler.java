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

import com.google.common.base.Nullable;

/**
 * Handles {@code Long}s.
 *
 * @author Steve Reed
 */
final class LongFixtureHandler extends NumberFixtureHandler<Long> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<Long> getPrimitiveType() {
		return Long.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public Long apply(@Nullable final Number number) {
		return number.longValue();
	}
}
