package io.github.kovalev.datafetcher.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


public interface CustomSpecification<E> extends Specification<E> {

    Specification<E> specification();

    @Override
    @Nullable
    default Predicate toPredicate(@NonNull Root<E> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        return specification().toPredicate(root, query, cb);
    }

}
