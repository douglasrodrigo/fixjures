package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.proxy.ObjectProxyData;
import com.google.common.base.Supplier;
import org.ho.yaml.Yaml;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import static com.bigfatgun.fixjures.FixtureException.convert;

public class YamlSource extends FixtureSource {

	public static FixtureSource newYamlResource(String resourceName) {
		return new YamlSource(YamlSource.class.getClassLoader().getResourceAsStream(resourceName));
	}

	public static FixtureSource newYamlStream(ReadableByteChannel channel) {
		return new YamlSource(Channels.newInputStream(channel));
	}

	private YamlSource(InputStream input) {
		super(Channels.newChannel(input));
	}

	@Override
	protected Object createFixture(FixtureType type) {
		try {
			Object object = Yaml.load(Channels.newInputStream(getSource()));
			final Supplier<?> provider = findValue(type, object);
			final Object value = provider.get();
			return type.getType().cast(value);
		} catch (Exception e) {
			throw convert(e);
		}
	}
}
