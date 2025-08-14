package io.github.kovalev.datafetcher;

import io.github.kovalev.datafetcher.specifications.And;
import io.github.kovalev.datafetcher.specifications.Empty;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface Query<T> {

    Pageable pageable();

    Filter<T> filter();

    Search<T> search();

    default Specification<T> specification() {
        val filter = filter() == null
                ? new Empty<T>()
                : filter().toSpecification();

        val search = search() == null
                ? new Empty<T>()
                : search().toSpecification();

        return new And<>(filter, search).specification();
    }
}
