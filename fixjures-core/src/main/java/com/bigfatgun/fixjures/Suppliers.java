package com.bigfatgun.fixjures;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Supplier;

public final class Suppliers {
	private Suppliers() {}

	private static final class StaticSupplier<T> implements Supplier<T> {

		private final T t;

		public StaticSupplier(@Nullable T t) {
			this.t = t;
		}

		public T get() {
			return t;
		}
	}

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
		return new StaticSupplier<T>(value);
	}

	public static <T> Supplier<T> ofIdentity(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
		return new IdentityResolvingSupplier<T>(identityResolver, type, rawIdentityValue);
	}
}
