package com.bigfatgun.fixjures.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class FixtureModule extends AbstractModule {
	@Override
	protected void configure() {
		bindListener(Matchers.any(), new FixtureTypeListener());
	}
}
