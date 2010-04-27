/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigfatgun.fixjures.yaml;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.proxy.ObjectProxyData;
import com.google.common.base.Supplier;
import org.ho.yaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;

import static com.bigfatgun.fixjures.FixtureException.convert;

public class YamlSource extends FixtureSource {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

	public static FixtureSource newYamlResource(String resourceName) {
		return new YamlSource(YamlSource.class.getClassLoader().getResourceAsStream(resourceName));
	}

	public static FixtureSource newYamlStream(ReadableByteChannel channel) {
		return new YamlSource(Channels.newInputStream(channel));
	}

    public static FixtureSource newYamlString(String yaml) {
        return new YamlSource(new ByteArrayInputStream(yaml.getBytes(UTF_8)));
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
