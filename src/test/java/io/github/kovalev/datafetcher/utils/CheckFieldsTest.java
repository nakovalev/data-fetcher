package io.github.kovalev.datafetcher.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckFieldsTest {

    @Test
    void nonNull() {
        assertThat(new CheckFields(null).nonNull()).isFalse();
        assertThat(new CheckFields(new String[] {}).nonNull()).isFalse();
        assertThat(new CheckFields(new String[] {null}).nonNull()).isFalse();
        assertThat(new CheckFields(new String[] {null, "field"}).nonNull()).isFalse();
        assertThat(new CheckFields(new String[] {"", "field2"}).nonNull()).isFalse();
        assertThat(new CheckFields(new String[] {"field1", "field2"}).nonNull()).isTrue();
    }
}