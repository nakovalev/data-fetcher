package io.github.kovalev.datafetcher.enums;

/**
 * Политика разрешения ситуаций, когда value == null.
 */
public enum NullPolicy {
    /**
     * не добавлять where
     */
    IGNORE,

    /**
     * добавить where field is (not) null
     */
    ADD_AS_NULL
}
