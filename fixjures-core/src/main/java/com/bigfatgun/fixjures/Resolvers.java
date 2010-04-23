package com.bigfatgun.fixjures;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Resolvers {
	private Resolvers() {}

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

	public static <T> Supplier<T> ofIdentity(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
		return new IdentityResolvingSupplier<T>(identityResolver, type, rawIdentityValue);
	}
}
