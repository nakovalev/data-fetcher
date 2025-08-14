package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.utils.CheckParams;
import io.github.kovalev.datafetcher.utils.PathCalculator;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class Include<E, I> implements CustomSpecification<E> {

    private final transient Collection<I> values;
    private final String[] fields;

    public Include(Collection<I> values, String... fields) {
        this.values = values;
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(values, fields).nonNull()) {
            return (root, query, cb) -> {
                val path = new PathCalculator<E, Collection<I>>(root, fields).path();
                return cb.in(path).value(values);
            };
        }

        return new Empty<>();
    }
}
