package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.utils.PathCalculator;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public class IsNotNull<E> implements CustomSpecification<E> {

    private final String[] fields;

    public IsNotNull(String... fields) {
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        return (root, query, cb) -> {
            Path<Object> path = new PathCalculator<>(root, fields).path();
            return cb.isNotNull(path);
        };
    }
}
