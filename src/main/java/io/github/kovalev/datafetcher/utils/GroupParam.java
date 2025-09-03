package io.github.kovalev.datafetcher.utils;

import io.github.kovalev.specificationhelper.utils.FieldsParser;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public final class GroupParam {

    private final ExpressionFunction expressionFunction;
    private final String[] fields;
    private String alias;

    public GroupParam(@NonNull String alias, @NonNull ExpressionFunction expressionFunction) {
        this.fields = null;
        this.alias = alias;
        this.expressionFunction = expressionFunction;
    }

    public GroupParam(@NonNull String... fields) {
        this.fields = new FieldsParser().parse(fields);
        this.alias = fields[0];
        this.expressionFunction = null;
    }

    public Expression<?> expression(@NonNull Root<?> root, @NonNull CriteriaBuilder cb) {
        Expression<?> expression;
        if (expressionFunction == null) {
            expression = new PathCalculator<>(root, fields).path();
        } else {
            expression = expressionFunction.apply(root, cb);
        }
        expression.alias(alias);
        return expression;
    }

    public GroupParam alias(String alias) {
        this.alias = alias;
        return this;
    }
}
