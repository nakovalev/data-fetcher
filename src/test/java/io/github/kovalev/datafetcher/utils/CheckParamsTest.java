package io.github.kovalev.datafetcher.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckParamsTest {

    @Test
    void nonNull() {
        assertThat(new CheckParams(null, null).nonNull()).isFalse();
        assertThat(new CheckParams("value", null).nonNull()).isFalse();
        assertThat(new CheckParams(null, new String[]{"field1", "field2"}).nonNull()).isFalse();
        assertThat(new CheckParams("value", new String[]{"field1", "field2"}).nonNull()).isTrue();
    }
}