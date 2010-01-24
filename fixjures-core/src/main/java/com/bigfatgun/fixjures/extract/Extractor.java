package com.bigfatgun.fixjures.extract;

import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Function;
import com.google.common.base.Functions;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class Extractor<F, T> implements Function<F, T> {

	public static Extractor<Object, Integer> ofHashCode() {
		return new Extractor<Object, Integer>() {{
			setDelegate(new Function<Object, Integer>() {
				@Override
				public Integer apply(@Nullable Object o) {
					return o.hashCode();
				}
			});
		}};
	}

	public static Extractor<Object, String> ofToString() {
		return new Extractor<Object, String>() {{
			setDelegate(Functions.toStringFunction());
		}};
	}
	
	public static <T extends Number> Extractor<T, Byte> ofByteValue() {
		return new Extractor<T, Byte>() {{
			setDelegate(new Function<T, Byte>() {
				@Override
				public Byte apply(@Nullable T t) {
					return t.byteValue();
				}
			});
		}};
	}

	private Function<F, T> delegate;

	private final InvocationHandler handler = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Extractor.this.record(method, args);
			return Defaults.defaultValue(method.getReturnType());
		}
	};

	final void setDelegate(final Function<F, T> delegate) {
		this.delegate = delegate;
	}

	final void record(final Method method, final Object[] args) {
		this.delegate = createReflectionFunction(method, args);
	}

	@SuppressWarnings({"unchecked"})
	protected final F execute(Class<F> cls) {
		return (F) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{ cls }, handler);
	}

	public final T apply(final F obj) {
		return this.delegate.apply(obj);
	}

	private Function<F, T> createReflectionFunction(final Method method, final Object[] args) {
		return new Function<F, T>() {
			@SuppressWarnings({"unchecked", "RedundantTypeArguments"})
			@Override
			public T apply(@Nullable F f) {
				try {
					return (T) method.invoke(f, args);
				} catch (Exception e) {
					return FixtureException.<T>convertAndThrowAs(e);
				}
			}
		};
	}
}
