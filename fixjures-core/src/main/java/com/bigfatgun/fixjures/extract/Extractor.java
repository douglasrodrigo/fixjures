package com.bigfatgun.fixjures.extract;

import com.bigfatgun.fixjures.FixtureException;
import com.google.common.base.Function;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class Extractor<T> implements Function<Object, T> {

	public static Extractor<Integer> ofHashCode() {
		return new Extractor<Integer>() {{
			try {
				this.args = new Object[0];
				this.method = Object.class.getMethod("hashCode");
			} catch (NoSuchMethodException e) {
				// shouldn't really happen?
			}
		}};
	}

	public static Extractor<String> ofToString() {
		return new Extractor<String>() {{
			try {
				this.args = new Object[0];
				this.method = Object.class.getMethod("toString");
			} catch (NoSuchMethodException e) {
				// shouldn't really happen?
			}
		}};
	}

	Method method;
	Object[] args;

	private final InvocationHandler handler = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Extractor.this.method = method;
			Extractor.this.args = args;
			return Defaults.defaultValue(method.getReturnType());
		}
	};

	@SuppressWarnings({"unchecked"})
	protected final <T> T execute(Class<T> cls) {
		return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{ cls }, handler);
	}

	@SuppressWarnings({"unchecked", "RedundantTypeArguments"})
	public final T apply(final Object obj) {
		try {
			return (T) method.invoke(obj, args);
		} catch (Exception e) {
			return FixtureException.<T>convertAndThrowAs(e);
		}
	}
}
