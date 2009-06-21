package com.bigfatgun.fixjures.handlers;

public interface PrimitiveHandler<T> extends FixtureHandler<T> {

	Class<? extends T> getPrimitiveType();
}
