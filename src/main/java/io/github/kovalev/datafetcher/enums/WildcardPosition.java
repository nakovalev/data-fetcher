package io.github.kovalev.datafetcher.enums;

/**
 * Настройка wildcard для оператора Like
 */
public enum WildcardPosition {
    /**
     * like '%value%'
     */
    BOTH,

    /**
     * like '%value'
     */
    START_ONLY,

    /**
     * like 'value%'
     */
    END_ONLY,

    /**
     * like 'value'
     */
    NONE
}
