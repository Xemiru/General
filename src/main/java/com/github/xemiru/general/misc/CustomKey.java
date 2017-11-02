package com.github.xemiru.general.misc;

/**
 * A generic identifier class.
 *
 * @param <T> the type of the object identified by the key
 */
public class CustomKey<T> {

    /**
     * Creates a new {@link CustomKey} resolving to the provided type parameter.
     *
     * @param <T> the type identified by the new CustomKey
     * @return the CustomKey
     */
    public static <T> CustomKey<T> key() {
        return new CustomKey<>();
    }

    /**
     * Creates a new {@link CustomKey} resolving to the provided type.
     *
     * @param type the type identified by the new CustomKey
     * @param <T> the type identified by the new CustomKey
     * @return the CustomKey
     */
    public static <T> CustomKey<T> key(Class<T> type) {
        return new CustomKey<>();
    }

}
