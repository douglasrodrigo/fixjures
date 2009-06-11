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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.ValueProvider;
import com.bigfatgun.fixjures.ValueProviders;

/**
 * Converts character sequences into {@code java.util.Date}s.
 *
 * @author Steve Reed
 */
class JavaDateHandler extends AbstractFixtureHandler<CharSequence, Date> {

	/**
	 * @return {@code java.util.Date.class}
	 */
	public Class<Date> getReturnType() {
		return Date.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<CharSequence> getSourceType() {
		return CharSequence.class;
	}

	/**
	 * {@inheritDoc}
	 */
	public ValueProvider<Date> apply(final HandlerHelper helper, final CharSequence charSequence) {
		try {
			return ValueProviders.of(DateFormat.getDateTimeInstance().parse(charSequence.toString()));
		} catch (ParseException e) {
			try {
				return ValueProviders.of(DateFormat.getDateInstance().parse(charSequence.toString()));
			} catch (ParseException e1) {
				try {
					return ValueProviders.of(DateFormat.getTimeInstance().parse(charSequence.toString()));
				} catch (ParseException e2) {
					throw new FixtureException("Failed to parse date: " + charSequence);
				}
			}
		}
	}
}
