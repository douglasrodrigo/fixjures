package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.annotations.SourceType;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public abstract class DAOHelper<T> {

	public static <T> DAOHelper<T> forClass(final Class<T> cls, final SourceFactory factory) {
		return new DAOHelper<T>(cls, factory) {};
	}

	public static <T> DAOHelper<T> forClassFromJSON(final Class<T> cls, final Map<String, String> objectMap) {
		final DAOHelper<T> helper = forClassFromJSON(cls, Strategies.newInMemoryStrategy(ImmutableMap.of(cls, objectMap)));
		helper.setIdentifiers(objectMap.keySet());
		return helper;
	}

	public static <T> DAOHelper<T> forClassFromJSON(final Class<T> cls, final Strategies.SourceStrategy strategy) {
		return forClass(cls, new AbstractSourceFactory(strategy) {
			@Override
			public FixtureSource newInstance(Class<?> fixtureType, String fixtureId) {
				try {
					return JSONSource.newJsonStream(strategy.findStream(fixtureType, fixtureId));
				} catch (IOException e) {
					return FixtureException.convertAndThrowAs(e);
				}
			}
		});
	}

	private final Class<T> cls;
	private final FixtureFactory factory;
	private final Set<String> identifiers;

	private DAOHelper(final Class<T> cls, final SourceFactory source) {
		this.cls = cls;
		this.factory = FixtureFactory.newFactory(source)
				.enableOption(Fixjure.Option.LAZY_REFERENCE_EVALUATION)
				.enableOption(Fixjure.Option.NULL_ON_UNMAPPED)
				.enableOption(Fixjure.Option.SKIP_UNMAPPABLE);
		this.identifiers = Sets.newHashSet();
	}

	public T findById(final String id) {
		return (identifiers.contains(id)) ? factory.createFixture(cls, id) : null;
	}

	public boolean addIdentifier(final String id) {
		return identifiers.add(id);
	}

	public boolean removeIdentifier(final String id) {
		return identifiers.remove(id);
	}

	public void setIdentifiers(Collection<String> newIdentifiers) {
		identifiers.clear();
		identifiers.addAll(newIdentifiers);
	}

	public Iterable<T> findAll() {
		return Iterables.transform(identifiers, new Function<String, T>() {
			@Override
			public T apply(@Nullable String id) {
				return findById(id);
			}
		});
	}
	
	public Iterable<T> findAllWhere(final Predicate<? super T> condition, final Predicate<? super T>... others) {
		final Predicate<T> allOthers = Predicates.<T>and(others);
		final Predicate<T> allConditions = Predicates.<T>and(condition, allOthers);
		return Iterables.filter(findAll(), allConditions);
	}

	public List<T> findAllOrdered(final Comparator<? super T> comparator) {
		List<T> list = Lists.newArrayList(findAll());
		Collections.sort(list, comparator);
		return list;
	}

	public List<T> findAllOrderedWhere(final Comparator<? super T> comparator, final Predicate<T> condition, final Predicate<T>... others) {
		List<T> list = Lists.newArrayList(findAllWhere(condition, others));
		Collections.sort(list, comparator);
		return list;
	}
}
