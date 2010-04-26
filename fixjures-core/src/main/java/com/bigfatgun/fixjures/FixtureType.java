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

package com.bigfatgun.fixjures;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * This {@code Type} generalizes the concept of an object class which may have some type parameters, and provides some
 * convenience methods for drilling into those parameters.
 */
public final class FixtureType implements Type {

	private final Class<?> type;
	private final ImmutableList<? extends Type> params;

	FixtureType(final Class<?> type, final ImmutableList<? extends Type> params) {
		this.type = checkNotNull(type);
		this.params = ImmutableList.copyOf(checkNotNull(params));
	}

	public Class<?> getType() { return type; }

	public ImmutableList<? extends Type> getParams() { return params; }

	public FixtureType keyType() {
		return TypeWrapper.wrap(params.size() > 0 ? params.get(0) : Object.class);
	}

	public FixtureType valueType() {
		return TypeWrapper.wrap(params.size() > 1 ? params.get(1) : Object.class);
	}

	public FixtureType collectionType() {
		return keyType();
	}

	public FixtureType of(final Type... params) {
		checkNotNull(params);
		final ImmutableList.Builder<Type> parameterBuilder = ImmutableList.builder();
		parameterBuilder.addAll(Arrays.asList(params));
		return new FixtureType(type, parameterBuilder.build());
	}

	public boolean isA(final Class<?> type) {
		return checkNotNull(type).isAssignableFrom(getType());
	}

	public FixtureType toSuper() {
		final Class<?> superClass = type.getSuperclass();
		return superClass == null ? null : new FixtureType(superClass, ImmutableList.<Type>of());
	}

	@Override
	public String toString() {
		if (getParams().isEmpty()) {
			return getType().toString();
		} else {
			return String.format("%s<%s>", getType(), getParams());
		}
	}
}
