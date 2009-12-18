package com.bigfatgun.fixjures;

import com.google.common.annotations.VisibleForTesting;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.ImmutableList;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class TypeWrapper {
	private TypeWrapper() {}

	public static FixtureType wrapMethodReturnType(final Method method) {
		checkNotNull(method);
		return wrap(method.getGenericReturnType());
	}

	public static FixtureType wrap(final Type type) {
		checkNotNull(type);

		if (type instanceof FixtureType) {
			return (FixtureType) type;
		} else {
			return wrapExternalType(type);
		}
	}

	private static FixtureType wrapExternalType(final Type type) {
		assert type != null : "Should have been null checked by now.";
		if (type instanceof ParameterizedType) {
			return wrapParameterizedType((ParameterizedType) type);
		} else if (type instanceof TypeVariable) {
			return wrapTypeVariable((TypeVariable) type);
		} else {
			return tryToWrapClass(type);
		}
	}

	@VisibleForTesting
	public static FixtureType wrapParameterizedType(final ParameterizedType type) {
		final Class<?> mainClass = (Class<?>) type.getRawType();
		final Type[] typeParams = type.getActualTypeArguments();
		return new FixtureType(mainClass, ImmutableList.of(typeParams));
	}

	@VisibleForTesting
	public static FixtureType wrapTypeVariable(final TypeVariable type) {
		return new FixtureType((Class<?>) type.getBounds()[0], ImmutableList.<Type>of());
	}

	@VisibleForTesting
	public static FixtureType tryToWrapClass(final Type type) {
		final Class<?> mainClass = (Class<?>) type;
		if (mainClass.isArray()) {
			return new FixtureType(mainClass, ImmutableList.of(mainClass.getComponentType()));
		} else {
			return new FixtureType(mainClass, ImmutableList.<Type>of());
		}
	}
}
