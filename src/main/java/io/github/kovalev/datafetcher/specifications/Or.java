package io.github.kovalev.datafetcher.specifications;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Or<E> implements CustomSpecification<E> {

    private final List<Specification<E>> specifications = new ArrayList<>();

    @SafeVarargs
    public Or(CustomSpecification<E>... specifications) {
        this.specifications.addAll(Arrays.asList(specifications));
    }

    public Or(Collection<Specification<E>> specifications) {
        this.specifications.addAll(specifications);
    }

    @Override
    public Specification<E> specification() {
        return specifications.stream()
                .reduce(Specification::or)
                .orElseGet(Empty::new);
    }
}
