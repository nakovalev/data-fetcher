package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.QueryField;
import org.springframework.lang.NonNull;

import java.util.List;

public class QueryFieldFactory {

    public QueryField field(@NonNull String field) {
        return new QueryField(field, null);
    }

    public List<QueryField> fields(@NonNull String field) {
        return List.of(field(field));
    }

    public QueryField deepField(@NonNull String... fields) {
        QueryField firstField = field(fields[0]);
        QueryField queryField = firstField;
        if (fields.length > 1) {
            for (int i = 1; i < fields.length; i++) {
                QueryField nextField = field(fields[i]);
                queryField.setChildFields(List.of(nextField));
                queryField = nextField;
            }
        }
        return firstField;
    }

    public List<QueryField> deepFields(@NonNull String... fields) {
        return List.of(deepField(fields));
    }
}
