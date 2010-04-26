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

import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureType;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts character sequences into {@code java.util.Date}s.
 *
 * @author Steve Reed
 */
class DateUnmarshaller extends AbstractUnmarshaller<Date> {

    private final Iterable<DateFormat> formats = ImmutableList.of(
            new SimpleDateFormat("yyyy-MM-dd"),
            DateFormat.getDateTimeInstance(),
            DateFormat.getDateInstance(),
            DateFormat.getTimeInstance()
    );

	public DateUnmarshaller() {
		super(CharSequence.class, Date.class);
	}

	public Supplier<? extends Date> unmarshall(final UnmarshallingContext helper, final Object source, final FixtureType typeDef) {

        if (source == null) {
            return null;
        }

        final CharSequence charSequence = castSourceValue(CharSequence.class, source);
        for (DateFormat format : formats) {
            try {
                return Suppliers.ofInstance(format.parse(charSequence.toString()));
            } catch (ParseException e) {
                // ignore
            }
        }

        throw new FixtureException("Failed to parse date: " + charSequence);
    }
}
