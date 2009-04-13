package com.bigfatgun.fixjures.sample;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Sample {

	public boolean containsFavoriteTeam(final League league) {
		return Iterables.any(league.getTeams(), new Predicate<Team>() {
			public boolean apply(@Nullable final Team team) {
				return team.getName().equals("Giants");
			}
		});
	}
}
