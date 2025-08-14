package io.github.kovalev.datafetcher.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.time.LocalDate;

public final class Expressions {

    public Expression<?> get(CriteriaBuilder cb, Expression<?> path, Object value) {
        return switch (value) {
            case LocalDate ignored -> cb.function("DATE", LocalDate.class, path);
            default -> path;
        };
    }

    public Expression<String> toLower(CriteriaBuilder cb, Expression<?> path) {
        return cb.lower(path.as(String.class));
    }
}
