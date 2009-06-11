package com.bigfatgun.fixjures;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ValueProviders {
	private ValueProviders() {}

	private static final class ValueProviderImpl<T> implements ValueProvider<T> {

		private final T t;

		public ValueProviderImpl(@Nullable T t) {
			this.t = t;
		}

		public T get() {
			return t;
		}
	}

	private static final class IdentityResolvingProvider<T> implements ValueProvider<T> {

		private final IdentityResolver identityResolver;
		private final Class<T> type;
		private final Object rawIdentityValue;

		public IdentityResolvingProvider(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
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

	public static <T> ValueProvider<T> of(@Nullable T value) {
		return new ValueProviderImpl<T>(value);
	}

	public static <T> ValueProvider<T> ofIdentity(final IdentityResolver identityResolver, final Class<T> type, final Object rawIdentityValue) {
		return new IdentityResolvingProvider<T>(identityResolver, type, rawIdentityValue);
	}
}
