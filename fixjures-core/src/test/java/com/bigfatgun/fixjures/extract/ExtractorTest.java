package com.bigfatgun.fixjures.extract;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExtractorTest {

	public static interface Dummy {
		public long getIt();
		public long halveIt();
	}

	private Dummy createDummy(final long value) {
		return new Dummy() {
			@Override
			public long getIt() {
				return value;
			}
			@Override
			public long halveIt() {
				return value / 2;
			}
		};
	}

	@Test
	public void byteValueOfInteger() {
		Extractor<Integer, Byte> intToByte = Extractor.ofByteValue();
		for (int i = -500; i < 500; i++) {
			assertEquals(new Byte((byte) i), intToByte.apply(i));
		}
	}

	@Test
	public void tryExtractorOnTheDummy() {
		long value = System.currentTimeMillis();
		Dummy dummy = createDummy(value);
		Extractor<Dummy, Long> x1 = new Extractor<Dummy, Long>() {{
			execute(Dummy.class).getIt();
		}};
		assertEquals(value, x1.apply(dummy).longValue());
		Extractor<Dummy, Long> x2 = new Extractor<Dummy, Long>() {{
			execute(Dummy.class).halveIt();
		}};
		assertEquals(value / 2, x2.apply(dummy).longValue());

		ArrayList<Dummy> dummies = Lists.newArrayList(createDummy(2), createDummy(4));
		List<Long> longs = Lists.newArrayList(Iterables.transform(dummies, x1));
		assertEquals(Lists.newArrayList(2L, 4L), longs);

		longs = Lists.newArrayList(Iterables.transform(dummies, x2));
		assertEquals(Lists.newArrayList(1L, 2L), longs);

		System.out.println(Iterables.transform(dummies, Extractor.ofHashCode()));
		System.out.println(Iterables.transform(dummies, Extractor.ofToString()));
	}
}
