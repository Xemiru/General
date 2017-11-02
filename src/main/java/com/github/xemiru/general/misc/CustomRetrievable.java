package com.github.xemiru.general.misc;

import java.util.Map;
import java.util.Optional;

/**
 * An object able to hold custom data.
 */
public interface CustomRetrievable {

    /**
     * Returns the mapping of custom properties held by this {@link CustomRetrievable}.
     *
     * <p>Changes to this map are reflected in the corresponding getter/setter methods. It is recommended to use the
     * former instead of modifying the returned map directly.</p>
     *
     * @return this CustomRetrievable's custom mapping
     */
    Map<CustomKey<?>, Object> getCustomMap();

    /**
     * Safely retrieves and returns a custom property set on this {@link CustomRetrievable}.
     *
     * <p>An empty Optional is returned if the value did not exist, or if the value did but was of the wrong type.</p>
     *
     * @param key the key of the property
     * @param <T> the type of the property
     * @return the property value?
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getCustomSafe(CustomKey<T> key) {
        try {
            return Optional.ofNullable(this.getCustom(key));
        } catch (ClassCastException ignored) {
        }

        return Optional.empty();
    }

    /**
     * Returns a custom property set on this {@link CustomRetrievable}.
     *
     * <p>This method has no safety and can throw a {@link ClassCastException} if the is casted into the wrong type by
     * the type parameter. Use {@link #getCustomSafe(CustomKey)} for null-safety and cast-safety.</p>
     *
     * @param key the key of the property
     * @param <T> the type of the property
     * @return the property value
     */
    default <T> T getCustom(CustomKey<T> key) {
        return (T) this.getCustomMap().get(key);
    }

}
