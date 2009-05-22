package com.bigfatgun.fixjures.guice;

import java.lang.reflect.Field;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.bigfatgun.fixjures.annotations.NativeSourceFormat;
import com.bigfatgun.fixjures.SourceFactory;
import com.bigfatgun.fixjures.FixjureFactory;
import com.google.inject.MembersInjector;

public class FixtureFieldInjector<T> implements MembersInjector<T> {

	private final Field f;
	private final Fixture fix;
	private final FixjureFactory factory;

	public FixtureFieldInjector(final Field field, final Fixture fixture) {
		f = field;
		fix = fixture;

		if (!f.isAccessible()) {
			f.setAccessible(true);
		}

		final NativeSourceFormat fmt = fix.format();
		final SourceFactory fact = fmt.createSourceFactory(fix);
		factory = FixjureFactory.newFactory(fact);
	}

	@Override
	public void injectMembers(final T t) {
		try {
			f.set(t, factory.createFixture(f.getType(), fix.name()));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
