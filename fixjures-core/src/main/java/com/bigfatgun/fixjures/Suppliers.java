package com.bigfatgun.fixjures;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;

public final class Suppliers {
	private Suppliers() {}

	private static final class IdentityResolvingSupplier<T> implements Supplier<T> {

		private final IdentityResolver identityResolver;
		private final Class<T> type;
		private final Object rawIdentityValue;

		public IdentityResolvingSupplier(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
			checkNotNull(identityResolver);
			checkNotNull(type);

			this.identityResolver = identityResolver;
			this.type = type;
			this.rawIdentityValue = rawIdentityValue;
		}

		public T get() {
			return identityResolver.resolve(type, identityResolver.coerceIdentity(rawIdentityValue));
		}
	}

	public static <T> Supplier<T> of(@Nullable T value) {
		return com.google.common.base.Suppliers.ofInstance(value);
	}

	public static <T> Supplier<T> ofIdentity(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
		return new IdentityResolvingSupplier<T>(identityResolver, type, rawIdentityValue);
	}

	public static <T> Supplier<ImmutableList<T>> ofImmutableList(final ImmutableList.Builder<T> builder) {
		return new Supplier<ImmutableList<T>>() {
			@Override
			public ImmutableList<T> get() {
				return builder.build();
			}
		};
	}

	public static <T> Supplier<ImmutableSet<T>> ofImmutableSet(final ImmutableSet.Builder<T> builder) {
		return new Supplier<ImmutableSet<T>>() {
			@Override
			public ImmutableSet<T> get() {
				return builder.build();
			}
		};
	}

	public static <K, V> Supplier<ImmutableMap<K, V>> ofImmutableMap(final ImmutableMap.Builder<K, V> builder) {
		return new Supplier<ImmutableMap<K, V>>() {
			@Override
			public ImmutableMap<K, V> get() {
				return builder.build();
			}
		};
	}
}
