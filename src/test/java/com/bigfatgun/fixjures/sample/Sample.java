package com.bigfatgun.fixjures.sample;

import com.google.common.collect.Iterables;
import com.google.common.base.Predicate;
import com.google.common.base.Nullable;

public class Sample {

	public boolean containsFavoriteTeam(final League league) {
		return Iterables.any(league.getTeams(), new Predicate<Team>() {
			public boolean apply(@Nullable final Team team) {
				return team.getName().equals("Giants");
			}
		});
	}
}
