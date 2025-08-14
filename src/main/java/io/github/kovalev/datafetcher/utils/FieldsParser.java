package io.github.kovalev.datafetcher.utils;

public class FieldsParser {

    public String[] parse(String... fields) {
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("fields is null or empty");
        }

        if (fields.length == 1 && fields[0].contains(".")) {
            return fields[0].split("\\.");
        }

        return fields;
    }
}
