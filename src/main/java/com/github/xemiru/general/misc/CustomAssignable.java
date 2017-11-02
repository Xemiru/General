package com.github.xemiru.general.misc;

/**
 * An object able to be assigned custom data.
 */
public interface CustomAssignable extends CustomRetrievable {

    /**
     * Sets a custom property on this {@link CustomAssignable}.
     *
     * @param key the key of the property
     * @param value the value of the property
     * @return this CustomAssignable
     */
    default <T> CustomAssignable setCustom(CustomKey<T> key, T value) {
        this.getCustomMap().put(key, value);
        return this;
    }

}
