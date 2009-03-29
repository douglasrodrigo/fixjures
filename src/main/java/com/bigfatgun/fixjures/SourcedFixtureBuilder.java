package com.bigfatgun.fixjures;

import java.util.IdentityHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * A "sourced" fixture builder, meaning it has at least the necessary state to begin
 * reading fixtures from some type of data.
 *
 * @param <T> fixture object type
 */
public abstract class SourcedFixtureBuilder<T> extends FixtureBuilder<T> {

	/**
	 * Map of desired value type to fixture handler.
	 */
	private final IdentityHashMap<Class, FixtureHandler> handlers;

	/**
	 * Protected constructor that stores the given builder's state.
	 *
	 * @param builder builder to copy
	 */
	protected SourcedFixtureBuilder(final FixtureBuilder<T> builder) {
		super(builder.getType());
		handlers = Maps.newIdentityHashMap();
	}

	/**
	 * Creates a new fixture object. This methid is implemented by subclasses which
	 * have an explicit knowledge of the fixture source format, such as the
	 * {@link com.bigfatgun.fixjures.json.JSONFixture}.
	 *
	 * @param handlers fixture handlers
	 * @return new object from fixture source
	 * @throws Exception if there is any sort of error reading the next object
	 */
	protected abstract Object createFixtureObject(final ImmutableMap<Class, FixtureHandler> handlers) throws Exception;

	/**
	 * Adds a fixture handler to this builder.
	 *
	 * @param handler handler to add
	 * @return this
	 */
	public SourcedFixtureBuilder<T> with(final FixtureHandler handler) {
		handlers.put(handler.getType(), handler);
		return this;
	}

	/**
	 * Strongly-typed fixture creation method. This method forwards calls to
	 * {@link SourcedFixtureBuilder#createFixtureObject(com.google.common.collect.ImmutableMap)} and then attempts to
	 * cast the object into the required type.
	 *
	 * @return new fixture object, or null if the object could not be created
	 */
	public final T create() {
		try {
			final Object obj = createFixtureObject(ImmutableMap.copyOf(handlers));
			if (getType().isAssignableFrom(obj.getClass())) {
				//noinspection unchecked
				return (T) obj;
			} else {
				Fixjure.warn("Invalid class! Expect " + getType() + " but got " + obj.getClass());
				return null;
			}
		} catch (Exception e) {
			Fixjure.warn("Error: " + e.getMessage());
			return null;
		}
	}
}
