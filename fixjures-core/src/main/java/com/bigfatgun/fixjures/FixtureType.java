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
		final ImmutableList.Builder<Type> paramBuilder = ImmutableList.builder();
		paramBuilder.addAll(Arrays.asList(params));
		return new FixtureType(type, paramBuilder.build());
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
