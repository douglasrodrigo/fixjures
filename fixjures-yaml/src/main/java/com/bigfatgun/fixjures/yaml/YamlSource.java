package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.handlers.AbstractUnmarshaller;
import com.bigfatgun.fixjures.handlers.UnmarshallingContext;
import com.bigfatgun.fixjures.proxy.ObjectProxy;
import com.bigfatgun.fixjures.proxy.Proxies;
import com.bigfatgun.fixjures.proxy.ProxyUtils;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.ho.yaml.Yaml;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import static com.bigfatgun.fixjures.FixtureException.convert;

public class YamlSource extends FixtureSource {

	static final class YamlMap implements Supplier<Map> {

		private final ImmutableMap<?, ?> fromYaml;

		public YamlMap(Map<?, ?> fromYaml) {
			this.fromYaml = ImmutableMap.copyOf(fromYaml);
		}

		public ImmutableMap<?, ?> getMapFromYaml() {
			return fromYaml;
		}

		@Override
		public Map get() {
			return getMapFromYaml();
		}
	}

	public static FixtureSource newYamlResource(String resourceName) {
		return new YamlSource(YamlSource.class.getClassLoader().getResourceAsStream(resourceName));
	}

	private YamlSource(InputStream input) {
		super(Channels.newChannel(input));
		installYamlHandlers();
	}

	private void installYamlHandlers() {
		installTypeHandler(YamlHandlers.newArrayHandler());
		installTypeHandler(YamlHandlers.newListHandler());
		installTypeHandler(YamlHandlers.newMapHandler());
		installTypeHandler(YamlHandlers.newMultisetHandler());
		installTypeHandler(YamlHandlers.newSetHandler());
		installTypeHandler(YamlHandlers.newObjectProxyHandler());
	}

	@Override
	protected Object createFixture(FixtureType type) {
		try {
			Object object = Yaml.load(Channels.newInputStream(getSource()));
			if (object instanceof Map) {
				object = new YamlMap((Map<?, ?>) object);
			}
			final Supplier<?> provider = findValue(type, object);
			final Object value = provider.get();
			return type.getType().cast(value);
		} catch (Exception e) {
			throw convert(e);
		}
	}
}
