package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.enums.WildcardPosition;
import io.github.kovalev.datafetcher.utils.CheckParams;
import io.github.kovalev.datafetcher.utils.Expressions;
import io.github.kovalev.datafetcher.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Objects;

public class Like<E> implements CustomSpecification<E> {

    private static final boolean DEFAULT_IGNORE_CASE = false;

    private final transient Object value;
    private final String[] fields;
    private final boolean ignoreCase;
    private final WildcardPosition wildcardPosition;
    private final transient Expressions expressions;

    public Like(Object value, String... fields) {
        this(value, WildcardPosition.BOTH, DEFAULT_IGNORE_CASE, fields);
    }

    public Like(Object value, boolean ignoreCase, String... fields) {
        this(value, WildcardPosition.BOTH, ignoreCase, fields);
    }

    public Like(Object value, @NonNull WildcardPosition wildcardPosition, String... fields) {
        this(value, wildcardPosition, DEFAULT_IGNORE_CASE, fields);
    }

    public Like(Object value, @NonNull WildcardPosition wildcardPosition, boolean ignoreCase, String... fields) {
        this.value = value;
        this.wildcardPosition = Objects.requireNonNull(wildcardPosition);
        this.ignoreCase = ignoreCase;
        this.fields = fields;
        this.expressions = new Expressions();
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(value, fields).nonNull()) {
            return (root, query, cb) -> {
                Path<Object> path = new PathCalculator<>(root, fields).path();
                Expression<String> stringExpression = expressions.get(cb, path, value).as(String.class);

                String pattern = applyWildcards(String.valueOf(value));

                if (ignoreCase) {
                    return cb.like(expressions.toLower(cb, stringExpression), pattern.toLowerCase(), '\\');
                }

                return cb.like(stringExpression, pattern, '\\');
            };
        }

        return new Empty<>();
    }

    private String applyWildcards(String value) {
        return switch (wildcardPosition) {
            case BOTH -> "%" + value + "%";
            case START_ONLY -> "%" + value;
            case END_ONLY -> value + "%";
            case NONE -> value;
        };
    }
}
