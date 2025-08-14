package io.github.kovalev.datafetcher.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldsParserTest {

    FieldsParser parser = new FieldsParser();

    @Test
    void shouldThrowExceptionWhenFieldsIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse(null));
        assertEquals("fields is null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldsIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> parser.parse());
        assertEquals("fields is null or empty", exception.getMessage());
    }

    @Test
    void shouldSplitSingleFieldByDot() {
        String[] result = parser.parse("order.item.price");
        assertArrayEquals(new String[]{"order", "item", "price"}, result);
    }

    @Test
    void shouldReturnSameArrayWhenMultipleFieldsWithoutDot() {
        String[] input = {"order", "item", "price"};
        String[] result = parser.parse(input);
        assertSame(input, result);
    }

    @Test
    void shouldNotSplitSingleFieldWithoutDot() {
        String[] result = parser.parse("status");
        assertArrayEquals(new String[]{"status"}, result);
    }
}