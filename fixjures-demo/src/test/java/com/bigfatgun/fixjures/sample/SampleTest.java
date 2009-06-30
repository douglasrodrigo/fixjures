package com.bigfatgun.fixjures.sample;

import java.net.URL;
import java.util.Map;

import com.bigfatgun.fixjures.Fixjure;
import static com.bigfatgun.fixjures.Fixjure.Option.SKIP_UNMAPPABLE;
import com.bigfatgun.fixjures.FixtureException;
import com.bigfatgun.fixjures.FixtureFactory;
import com.bigfatgun.fixjures.FixtureSource;
import com.bigfatgun.fixjures.Strategies;
import com.bigfatgun.fixjures.annotations.Fixture;
import static com.bigfatgun.fixjures.annotations.NativeSourceType.Literal;
import static com.bigfatgun.fixjures.annotations.NativeSourceType.Resource;
import com.bigfatgun.fixjures.guice.GuiceFixtureSuite;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.collect.ImmutableMap;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceFixtureSuite.class)
public class SampleTest {

	@Fixture(type = Resource, value = "fakeLeague.json")
	private League league;

	@Fixture(type = Literal, value = "{ name : 'MFL' }")
	private League other;

	@Test
	public void passTheLeagueAround() {
		// You can pass this object around to other methods for test
		assertNotNull(league);
		assertTrue(new Sample().containsFavoriteTeam(league));
	}

	@Test
	public void thatOtherLeague() {
		// not that this is the type of test you'd use with fixjures...
		// this is just to illustrate the most terse usage
		assertEquals("MFL", other.getName());
	}

	private static interface NyTimesChannel {
		String getTitle();
	}

	private static interface NyTimes {
		NyTimesChannel getChannel();
		String getVersion();
	}

	@Test
	public void urlFixture() {
		try {
			final String nytimesJsonUrl = "http://prototype.nytimes.com/svc/widgets/dataservice.html?uri=http://www.nytimes.com/services/xml/rss/nyt/World.xml";
			final FixtureSource src = JSONSource.newRemoteUrl(new URL(nytimesJsonUrl));
			NyTimes nytimes = Fixjure.of(NyTimes.class).from(src).withOptions(SKIP_UNMAPPABLE).create();
			System.out.println("Successfully connected to nytimes, got version: " + nytimes.getVersion());
			assertEquals("NYT > World", nytimes.getChannel().getTitle());
		} catch (Exception e) {
			e.printStackTrace();
			fail("You need access to nytimes.com for this test to work.");
		}
	}

	@Test
	public void objectReferenceTest() {
		final Map<String, String> leagues = ImmutableMap.of("nfl", "{ name: 'NFL' }");
		final Map<String, String> teams = ImmutableMap.of("the team", "{ name: 'The Team', league: 'nfl' }");
		final Map<Class<?>, Map<String, String>> json = ImmutableMap.of(
				  League.class, leagues,
				  Team.class, teams
				  );
		final FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(json));
		final Team t = fact.createFixture(Team.class, "the team");
		assertNotNull(t);
		assertEquals("The Team", t.getName());
		assertNotNull(t.getLeague());
		assertEquals("NFL", t.getLeague().getName());
	}

	@Test
	public void nullReferenceTest() {
		final Map<String, String> teams = ImmutableMap.of("the team", "{ name: 'The Team' }");
		final Map<Class<?>, Map<String, String>> json = ImmutableMap.<Class<?>, Map<String, String>>of(
				  Team.class, teams
				  );
		final FixtureFactory fact = FixtureFactory
				  .newJsonFactory(Strategies.newInMemoryStrategy(json))
				  .enableOption(Fixjure.Option.NULL_ON_UNMAPPED);
		final Team t = fact.createFixture(Team.class, "the team");
		assertNull(t.getLeague());
	}

	@Test(expected = FixtureException.class)
	public void nullReferenceTestFailsByDefault() {
		final Map<String, String> teams = ImmutableMap.of("the team", "{ name: 'The Team' }");
		final Map<Class<?>, Map<String, String>> json = ImmutableMap.<Class<?>, Map<String, String>>of(
				  Team.class, teams
				  );
		final FixtureFactory fact = FixtureFactory.newJsonFactory(Strategies.newInMemoryStrategy(json));
		final Team t = fact.createFixture(Team.class, "the team");
		assertNull(t.getLeague());
	}
}
