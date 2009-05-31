package com.bigfatgun.fixjures.sample;

import javax.annotation.Nullable;

import com.bigfatgun.fixjures.annotations.Fixture;
import static com.bigfatgun.fixjures.annotations.NativeSourceType.Literal;
import static com.bigfatgun.fixjures.annotations.NativeSourceType.Resource;
import com.bigfatgun.fixjures.guice.GuiceFixtureSuite;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceFixtureSuite.class)
public class SampleTest {

	private static void log(final League l) {
		System.out.println(Iterables.transform(l.getTeams(), new Function<Team, String>() {
			public String apply(@Nullable final Team team) {
				return String.format("%n%s%n\t%s%n", team.getName(), Iterables.transform(team.getPlayers(), new Function<Player, Object>() {
					public Object apply(@Nullable final Player player) {
						return String.format("#%d", player.getNumber());
					}
				}));
			}
		}));
	}

	@Fixture(type = Resource, value = "fakeLeague.json")
	private League league;

	@Fixture(type = Literal, value = "{ name : 'MFL' }")
	private League other;

	@Test
	public void passTheLeagueAround() {
		// You can pass this object around to other methods for test
		assertNotNull(league);
		log(league);
		assertTrue(new Sample().containsFavoriteTeam(league));
	}

	@Test
	public void thatOtherLeague() {
		// not that this is the type of test you'd use with fixjures...
		// this is just to illustrate the most terse usage
		assertEquals("MFL", other.getName());
	}
}
