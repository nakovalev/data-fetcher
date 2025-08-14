package io.github.kovalev.datafetcher.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CheckValueTest {

    @Test
    void nonNull() {
        assertThat(new CheckValue(null).nonNull()).isFalse();
        assertThat(new CheckValue(Integer.MAX_VALUE).nonNull()).isTrue();

        assertThat(new CheckValue("").nonNull()).isFalse();
        assertThat(new CheckValue("any").nonNull()).isTrue();

        assertThat(new CheckValue(List.of()).nonNull()).isFalse();
        assertThat(new CheckValue(List.of(Integer.MAX_VALUE)).nonNull()).isTrue();
        assertThat(new CheckValue(onlyNullValuesList()).nonNull()).isFalse();
        assertThat(new CheckValue(withNullValueList()).nonNull()).isTrue();
    }

    List<Integer> withNullValueList() {
        return new ArrayList<>() {{
            add(null);
            add(Integer.MAX_VALUE);
            add(Integer.MIN_VALUE);
        }};
    }

    List<Integer> onlyNullValuesList() {
        return new ArrayList<>() {{
            add(null);
        }};
    }
}