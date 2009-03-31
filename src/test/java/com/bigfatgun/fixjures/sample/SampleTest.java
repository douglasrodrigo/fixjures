package com.bigfatgun.fixjures.sample;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import com.bigfatgun.fixjures.json.JSONFixture;
import com.bigfatgun.fixjures.json.JSONFixtureHelper;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.base.Function;
import com.google.common.base.Nullable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class SampleTest {

	private League league;

	@JSONFixture(type = JSONSource.SourceType.FILE, value = "src/test/java/com/bigfatgun/fixjures/sample/fakeLeague.json")
	public void setLeague(final League value) {
		league = value;
	}

	@Before
	public void setUp() throws InvocationTargetException, FileNotFoundException, IllegalAccessException {
		JSONFixtureHelper.scan(this);
	}

	@Test
	public void testTheFakeLeague() {
		assertNotNull(league);
		assertEquals(4, league.getTeams().size());
		assertEquals(
				  ImmutableSet.of("Giants", "Raiders", "Mets", "Sounders FC"),
				  ImmutableSet.copyOf(Iterables.transform(league.getTeams(), new Function<Team, String>() {
					  public String apply(@Nullable final Team team) {
						  return team.getName();
					  }
				  })));
	}
}
