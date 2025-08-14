package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

public class IsNull<E> implements CustomSpecification<E> {

    private final String[] fields;

    public IsNull(String... fields) {
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        return (root, query, cb) -> {
            val path = new PathCalculator<>(root, fields).path();
            return cb.isNull(path);
        };
    }
}
