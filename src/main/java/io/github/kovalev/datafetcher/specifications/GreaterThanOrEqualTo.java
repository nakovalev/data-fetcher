package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.utils.CheckParams;
import io.github.kovalev.datafetcher.utils.Expressions;
import io.github.kovalev.datafetcher.utils.PathCalculator;
import jakarta.persistence.criteria.Expression;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

public class GreaterThanOrEqualTo<E, C extends Comparable<? super C>>
        implements CustomSpecification<E> {

    private final transient C value;
    private final String[] fields;

    public GreaterThanOrEqualTo(C value, String... fields) {
        this.value = value;
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(value, fields).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<>(root, fields).path();
                val expression = (Expression<C>) new Expressions().get(cb, path, value);
                return cb.greaterThanOrEqualTo(expression, value);
            };
        }

        return new Empty<>();
    }
}
