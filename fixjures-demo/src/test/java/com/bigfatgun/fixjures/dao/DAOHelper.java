package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;

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
	private final Function<String, T> loadByIdFunction;

	private DAOHelper(final Class<T> cls, final SourceFactory source) {
		this.cls = cls;
		this.factory = FixtureFactory.newFactory(source)
				.enableOption(Fixjure.Option.LAZY_REFERENCE_EVALUATION)
				.enableOption(Fixjure.Option.NULL_ON_UNMAPPED)
				.enableOption(Fixjure.Option.SKIP_UNMAPPABLE);
		this.identifiers = Sets.newHashSet();
		this.loadByIdFunction = new Function<String, T>() {
			@Override
			public T apply(@Nullable String s) {
				return DAOHelper.this.findById(s);
			}
		};
	}

	public T findById(final String id) {
		return (identifiers.contains(id)) ? factory.createFixture(cls, id) : null;
	}

	public T add(final T object, final String identifier) {
		addIdentifier(identifier);
		return factory.cache(cls, object, identifier);
	}

	public T remove(final String identifier) {
		removeIdentifier(identifier);
		return factory.uncache(cls, identifier);
	}

	private boolean addIdentifier(final String id) {
		return identifiers.add(id);
	}

	private boolean removeIdentifier(final String id) {
		return identifiers.remove(id);
	}

	public void setIdentifiers(Collection<String> newIdentifiers) {
		identifiers.clear();
		identifiers.addAll(newIdentifiers);
	}

	public Iterable<T> findAll() {
		return Iterables.transform(identifiers, loadByIdFunction);
	}
	
	@SuppressWarnings({"RedundantTypeArguments"})
	public Iterable<T> findAllWhere(final Predicate<? super T> condition, final Predicate<? super T>... others) {
		final Predicate<T> allOthers = Predicates.<T>and(others);
		final Predicate<T> allConditions = Predicates.<T>and(condition, allOthers);
		return Iterables.filter(findAll(), allConditions);
	}

	public int findIndexOfObjectInOrder(final T object, final Ordering<? super T> ordering) {
		return ordering.binarySearch(ordering.sortedCopy(findAll()), object);
	}

	public List<T> findAllOrderedAscending(final Ordering<? super T> ordering) {
		return ordering.sortedCopy(findAll());
	}

	public List<T> findAllOrderedDescending(final Ordering<? super T> ordering) {
		return ordering.reverse().sortedCopy(findAll());
	}

	public List<T> findAllOrderedAscendingWhere(final Ordering<? super T> ordering, final Predicate<T> condition, final Predicate<T>... others) {
		return ordering.sortedCopy(findAllWhere(condition, others));
	}

	public List<T> findAllOrderedDescendingWhere(final Ordering<? super T> ordering, final Predicate<T> condition, final Predicate<T>... others) {
		return ordering.reverse().sortedCopy(findAllWhere(condition, others));
	}
}
