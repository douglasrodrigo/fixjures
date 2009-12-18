package com.bigfatgun.fixjures;

import javax.annotation.Nullable;

/** Interface marks an entity that is able to find and create/retrieve an object stub by type and id. */
public interface IdentityResolver {

	/**
	 * @param requiredType object type
	 * @param rawIdentityValue object id in raw form
	 * @return true if this provider can resolve the given identity value
	 */
	boolean canHandleIdentity(Class<?> requiredType, @Nullable Object rawIdentityValue);

	/**
	 * Converts the raw identity value into a string.
	 *
	 * @param rawIdentityValue raw identity value
	 * @return identty as string
	 */
	String coerceIdentity(@Nullable Object rawIdentityValue);

	/**
	 * Resolves an object by type and id.
	 *
	 * @param requiredType object type
	 * @param id object id
	 * @return object; if not found, null is returned
	 */
	<T> T resolve(Class<T> requiredType, String id);
}
