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
package com.bigfatgun.fixjures;

/**
 * Implemented by classes that can instantiate fixture sources by id.
 *
 * @author Steve Reed
 */
public interface SourceFactory {

	/**
	 * Creates a new instance of a fixture source, ready to create a fixture of the given id.
	 *
	 * @param fixtureType fixture object type
	 * @param fixtureId fixture object name or identifier
	 * @return fixture source
	 */
	FixtureSource newInstance(Class<?> fixtureType, String fixtureId);
}
