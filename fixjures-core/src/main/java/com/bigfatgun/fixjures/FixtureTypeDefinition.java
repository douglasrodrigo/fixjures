package com.bigfatgun.fixjures;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;

public final class FixtureTypeDefinition implements Type {

	public static FixtureTypeDefinition wrapMethodReturnType(final Method method) {
		return wrap(method.getGenericReturnType());
	}

	public static FixtureTypeDefinition wrap(final Type type) {
		if (type instanceof FixtureTypeDefinition) {
			return (FixtureTypeDefinition) type;
		}

		final Class<Class> classOfClass = Class.class;
		final Class<?> mainClass;
		final Type[] typeParams;
		if (type instanceof ParameterizedType) {
			mainClass = classOfClass.cast(((ParameterizedType) type).getRawType());
			typeParams = ((ParameterizedType) type).getActualTypeArguments();
		} else if (type instanceof TypeVariable) {
			final TypeVariable typeVar = (TypeVariable) type;
			mainClass = (Class<?>) typeVar.getBounds()[0];
			typeParams = new Type[0];
		} else {
			mainClass = classOfClass.cast(type);
			if (mainClass.isArray()) {
				typeParams = new Type[] { mainClass.getComponentType() };
			} else {
				typeParams = new Type[0];
			}
		}
		return new FixtureTypeDefinition(mainClass, ImmutableList.of(typeParams));
	}

	private final Class<?> type;
	private final ImmutableList<? extends Type> params;

	private FixtureTypeDefinition(final Class<?> type, final ImmutableList<? extends Type> params) {
		this.type = type;
		this.params = params;
	}

	public Class<?> getType() { return type; }

	public ImmutableList<? extends Type> getParams() { return params; }

	public FixtureTypeDefinition of(final Type... params) {
		final ImmutableList.Builder<Type> paramBuilder = ImmutableList.builder();
		paramBuilder.addAll(Arrays.asList(params));
		return new FixtureTypeDefinition(type, paramBuilder.build());
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
		if (getParams().isEmpty()) {
			return getType().toString();
		} else {
			return String.format("%s<%s>", getType(), getParams());
		}
	}

	public FixtureTypeDefinition keyTypeDefinition() {
		return wrap(keyType());
	}

	public FixtureTypeDefinition valueTypeDefinition() {
		return wrap(valueType());
	}

	public FixtureTypeDefinition collectionTypeDefinition() {
		return keyTypeDefinition();
	}
}
