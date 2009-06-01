package com.bigfatgun.fixjures.sample;

import java.util.Set;

public interface Team {

	String getName();

	Set<Player> getPlayers();

	League getLeague();
}
