package com.bigfatgun.fixjures.proxy;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ObjectProxyData {

	private final ImmutableMap<?, ?> data;

	public ObjectProxyData(Map<?, ?> map) {
		this.data = ImmutableMap.copyOf(map);
	}

	public ImmutableMap<?, ?> get() {
		return data;
	}
}
