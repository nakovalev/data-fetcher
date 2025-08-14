package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.utils.CheckParams;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;

public class Between<E, C extends Comparable<? super C>>
        implements CustomSpecification<E> {

    private final transient List<C> values;
    private final String[] fields;

    public Between(List<C> values, String... fields) {
        this.values = values;
        this.fields = fields;
    }

    @Override
    public Specification<E> specification() {
        if (new CheckParams(values, fields).nonNull()) {
            if (values.size() < 2 && values.stream().allMatch(Objects::isNull)) {
                return new Empty<>();
            }

            C from = values.get(0);
            C to = values.get(1);

            if (from != null && to == null) {
                return new GreaterThanOrEqualTo<E, C>(from, fields).specification();
            } else if (from == null && to != null) {
                return new LessThanOrEqualTo<E, C>(to, fields).specification();
            }

            return new And<E>(new GreaterThanOrEqualTo<>(from, fields), new LessThanOrEqualTo<>(to, fields))
                    .specification();
        }

        return new Empty<>();
    }
}
