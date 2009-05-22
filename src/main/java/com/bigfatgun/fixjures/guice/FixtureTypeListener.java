package com.bigfatgun.fixjures.guice;

import java.lang.reflect.Field;

import com.bigfatgun.fixjures.annotations.Fixture;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class FixtureTypeListener implements TypeListener {
	@Override
	public <I> void hear(final TypeLiteral<I> typeLiteral, final TypeEncounter<I> encounter) {
		for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
        if (field.isAnnotationPresent(Fixture.class)) {
			  final Fixture fixture = field.getAnnotation(Fixture.class);
			  encounter.register(new FixtureFieldInjector<I>(field, fixture));
		  }
      }
	}
}
