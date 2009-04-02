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
 * Handles {@code Byte}s.
 *
 * @author Steve Reed
 */
public final class ByteFixtureHandler extends NumberFixtureHandler<Byte> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<Byte> getPrimitiveType() {
		return Byte.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<Byte> getReturnType() {
		return Byte.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public Byte apply(@Nullable final Number number) {
		return number.byteValue();
	}
}
