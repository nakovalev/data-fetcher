package io.github.kovalev.datafetcher.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

@FunctionalInterface
public interface ExpressionFunction {

    Expression<?> apply(Root<?> root, CriteriaBuilder cb);
}