package com.bigfatgun.fixjures.sample;

import javax.annotation.Nullable;

import com.bigfatgun.fixjures.FixtureInjector;
import com.bigfatgun.fixjures.annotations.Fixture;
import com.bigfatgun.fixjures.annotations.NativeSourceType;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SampleTest {

	private League league;

	@Fixture(type = NativeSourceType.File, value = "src/test/java/com/bigfatgun/fixjures/sample/fakeLeague.json")
	public void setLeague(final League value) {
		league = value;
	}

	@Before
	public void setUp() throws Exception {
		FixtureInjector.scan(this);
	}

	private void log() {
		System.out.println(Iterables.transform(league.getTeams(), new Function<Team, String>() {
			public String apply(@Nullable final Team team) {
				return String.format("%n%s%n\t%s%n", team.getName(), Iterables.transform(team.getPlayers(), new Function<Player, Object>() {
					public Object apply(@Nullable final Player player) {
						return String.format("#%d", player.getNumber());
					}
				}));
			}
		}));
	}

	@Test
	public void passTheLeagueAround() {
		log();
		// You can pass this object around to other methods for test
		assertTrue(new Sample().containsFavoriteTeam(league));
	}
}
