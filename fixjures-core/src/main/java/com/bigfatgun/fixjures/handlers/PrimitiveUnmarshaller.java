package com.bigfatgun.fixjures.handlers;

public interface PrimitiveUnmarshaller<T> extends Unmarshaller<T> {

	Class<? extends T> getPrimitiveType();
}
