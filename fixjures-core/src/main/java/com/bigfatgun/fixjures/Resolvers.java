/*
 * Copyright (c) 2010 Steve Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
