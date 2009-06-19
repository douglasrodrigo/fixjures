package com.bigfatgun.fixjures;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;

public final class FixtureTypeDefinition<T> {

	public static <T> FixtureTypeDefinition<T> newDefinition(final Class<T> type) {
		return newDefinition(type, ImmutableList.<Class<?>>of());
	}

	public static <T> FixtureTypeDefinition<T> newDefinition(final Class<T> type, final ImmutableList<? extends Type> params) {
		return new FixtureTypeDefinition<T>(type, params);
	}

	private final Class<T> type;
	private final ImmutableList<? extends Type> params;

	private FixtureTypeDefinition(final Class<T> type, final ImmutableList<? extends Type> params) {
		this.type = type;
		this.params = params;
	}

	public Class<T> getType() { return type; }

	public ImmutableList<? extends Type> getParams() { return params; }

	public FixtureTypeDefinition<T> addParams(final Type[] params) {
		final ImmutableList.Builder<Type> paramBuilder = ImmutableList.builder();
		paramBuilder.addAll(this.params);
		paramBuilder.addAll(Arrays.asList(params));
		return newDefinition(type, paramBuilder.build());
	}

	public Type keyType() {
		return params.size() > 0 ? params.get(0) : Object.class;
	}

	public Type valueType() {
		return params.size() > 1 ? params.get(1) : Object.class;
	}

	public Type collectionType() {
		return keyType();
	}

	public boolean isA(final Class<?> type) {
		return type.isAssignableFrom(getType());
	}

	@Override
	public String toString() {
		return String.format("%s<%s>", getType(), getParams());
	}
}
