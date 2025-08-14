package io.github.kovalev.datafetcher.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.function.Function;
import java.util.function.UnaryOperator;

@Getter
public final class FunctionParams {

    private final Function<Object, Object> mapper;
    private final String functionName;
    private final Class<?> returnType;
    private final ExpressionFunction expressionFunction;
    private final String[] fields;
    private String alias;

    public FunctionParams(@NonNull String alias,
                          @NonNull UnaryOperator<Object> mapper,
                          @NonNull ExpressionFunction expressionFunction) {
        this.functionName = null;
        this.returnType = null;
        this.fields = null;
        this.mapper = mapper;
        this.expressionFunction = expressionFunction;
        this.alias = alias;
    }

    public FunctionParams(@NonNull String alias, @NonNull ExpressionFunction expressionFunction) {
        this.functionName = null;
        this.returnType = null;
        this.fields = null;
        this.mapper = Function.identity();
        this.expressionFunction = expressionFunction;
        this.alias = alias;
    }

    public FunctionParams(@NonNull String alias,
                          String functionName,
                          Class<?> returnType,
                          UnaryOperator<Object> mapper,
                          @NonNull String... fields) {
        this.functionName = functionName;
        this.returnType = returnType;
        this.mapper = mapper;
        this.fields = new FieldsParser().parse(fields);
        this.expressionFunction = null;
        this.alias = alias;
    }

    public Expression<?> expression(@NonNull Root<?> root, @NonNull CriteriaBuilder cb) {
        Expression<?> expression;
        if (expressionFunction == null) {
            Path<Object> path = new PathCalculator<>(root, fields).path();
            expression = cb.function(functionName, returnType, path);
        } else {
            expression = expressionFunction.apply(root, cb);
        }
        expression.alias(alias);
        return expression;
    }

    public FunctionParams alias(String alias) {
        this.alias = alias;
        return this;
    }
}
