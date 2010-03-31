package com.bigfatgun.fixjures.dao;

import com.bigfatgun.fixjures.*;
import com.bigfatgun.fixjures.json.JSONSource;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DAOHelper<T> {

	private static final class EmptyByteChannel implements ReadableByteChannel {
		@Override
		public int read(ByteBuffer dst) throws IOException {
			return -1;
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public void close() throws IOException {
			// ignore
		}
	}

	private static class ImmutableIdentifierProvider implements IdentifierProvider {

		private final ImmutableList<String> ids;

		public ImmutableIdentifierProvider(Iterable<String> identifiers) {
			ids = ImmutableList.copyOf(identifiers);
		}

		@Override
		public Iterable<String> existingObjectIdentifiers() {
			return ids;
		}
	}
	
	private static final class EmptyIdentifierProvider extends ImmutableIdentifierProvider {

		public EmptyIdentifierProvider() {
			super(ImmutableList.<String>of());
		}
	}

	public static <T> DAOHelper<T> forClass(final Class<T> cls) {
		return forClass(cls, new SourceFactory() {
			@Override
			public FixtureSource newInstance(Class<?> fixtureType, String fixtureId) {
				return new FixtureSource(new EmptyByteChannel()) {
					@Override
					protected Object createFixture(FixtureType type) {
						return null;
					}
				};
			}
		}, new EmptyIdentifierProvider());
	}

	public static <T> DAOHelper<T> forClass(final Class<T> cls, final SourceFactory factory, final IdentifierProvider idProvider) {
		return new DAOHelper<T>(cls, factory, idProvider) {
		};
	}

	public static <T> DAOHelper<T> forClassFromJSON(final Class<T> cls, final Map<String, String> objectMap) {
		return forClassFromJSON(cls, Strategies.newInMemoryStrategy(ImmutableMap.of(cls, objectMap)), new ImmutableIdentifierProvider(objectMap.keySet()));
	}

	public static <T> DAOHelper<T> forClassFromJSON(final Class<T> cls, final Strategies.SourceStrategy strategy, final IdentifierProvider idProvider) {
		return forClass(cls, new AbstractSourceFactory(strategy) {
			@Override
			public FixtureSource newInstance(Class<?> fixtureType, String fixtureId) {
				try {
					return JSONSource.newJsonStream(strategy.findStream(fixtureType, fixtureId));
				} catch (IOException e) {
					return FixtureException.convertAndThrowAs(e);
				}
			}
		}, idProvider);
	}

	private final Class<T> cls;
	private final FixtureFactory factory;
	private final Set<String> identifiers;
	private final Function<String, T> loadByIdFunction;

	private DAOHelper(final Class<T> cls, final SourceFactory source, final IdentifierProvider idProvider) {
		this.cls = cls;
		this.factory = FixtureFactory.newFactory(source)
				.enableOption(Fixjure.Option.LAZY_REFERENCE_EVALUATION)
				.enableOption(Fixjure.Option.NULL_ON_UNMAPPED)
				.enableOption(Fixjure.Option.SKIP_UNMAPPABLE);
		this.identifiers = Sets.newHashSet(idProvider.existingObjectIdentifiers());
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

	private void setIdentifiers(Collection<String> newIdentifiers) {
		identifiers.clear();
		identifiers.addAll(newIdentifiers);
	}

	public Iterable<T> findAll() {
		return Iterables.transform(identifiers, loadByIdFunction);
	}

	public Iterable<T> findAllWhere(final Predicate<? super T> condition) {
		return Iterables.filter(findAll(), condition);
	}

	public int findIndexOfObjectInOrder(final T object, final Ordering<? super T> ordering) {
		return ordering.binarySearch(findAllOrdered(ordering), object);
	}

	public List<T> findAllOrdered(final Ordering<? super T> ordering) {
		return ordering.sortedCopy(findAll());
	}

	public List<T> findAllOrderedWhere(final Ordering<? super T> ordering, final Predicate<? super T> condition) {
		return findAllOrderedWhere(ordering, condition, true);
	}

	public List<T> findAllOrderedWhere(final Ordering<? super T> ordering, final Predicate<? super T> condition, boolean filterBeforeSort) {
		if (filterBeforeSort) {
			return ordering.sortedCopy(findAllWhere(condition));
		} else {
			return Lists.newArrayList(Iterables.filter(findAllOrdered(ordering), condition));
		}
	}

    public FixtureFactory getFactory() {
        return factory;
    }
}
