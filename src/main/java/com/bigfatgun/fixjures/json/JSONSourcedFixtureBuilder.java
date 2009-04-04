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
package com.bigfatgun.fixjures.json;

import com.bigfatgun.fixjures.FixtureBuilder;
import com.bigfatgun.fixjures.SourcedFixtureBuilder;

/**
 * A fixture builder with a {@link JSONSource} source.
 *
 * @param <T> fixture object type
 * @author Steve Reed
 */
/* package */ final class JSONSourcedFixtureBuilder<T> extends SourcedFixtureBuilder<T, JSONSource> {

	/**
	 * Instantiates a new builder, inheriting the state of the given builder, and adding
	 * the JSON source.
	 *
	 * @param jsonSource json source
	 * @param builder	  fixture builder
	 */
	public JSONSourcedFixtureBuilder(final JSONSource jsonSource, final FixtureBuilder<T> builder) {
		super(builder, jsonSource);
	}

	/**
	 * Forwards to {@link JSONSource#createFixture(Class, java.util.List)}
	 * to create a fixture object.
	 *
	 * @return fixture object
	 */
	@Override
	protected T createFixtureObject() {
		return getSource().createFixture(getType(), getTypeParams());
	}
}
