package com.bigfatgun.fixjures.guice;

import java.lang.reflect.Field;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.bigfatgun.fixjures.annotations.NativeSourceFormat;
import com.bigfatgun.fixjures.SourceFactory;
import com.bigfatgun.fixjures.FixtureFactory;
import com.bigfatgun.fixjures.Fixjure;
import com.google.inject.MembersInjector;

public class FixtureFieldInjector<T> implements MembersInjector<T> {

	private final Field f;
	private final Fixture fix;
	private final FixtureFactory factory;

	public FixtureFieldInjector(final Field field, final Fixture fixture) {
		f = field;
		fix = fixture;

		if (!f.isAccessible()) {
			f.setAccessible(true);
		}

		final NativeSourceFormat fmt = fix.format();
		final SourceFactory fact = fmt.createSourceFactory(field.getDeclaringClass().getClassLoader(), fix.type());
		factory = FixtureFactory.newFactory(fact)
				  .enableOption(Fixjure.Option.SKIP_UNMAPPABLE)
				  .enableOption(Fixjure.Option.LAZY_REFERENCE_EVALUATION);
	}

	public void injectMembers(final T t) {
		try {
			f.set(t, factory.createFixture(f.getType(), fix.value()));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
