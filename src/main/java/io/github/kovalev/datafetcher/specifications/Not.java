package io.github.kovalev.datafetcher.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Класс для создания спецификации "НЕ".
 *
 * @param <E> тип сущности
 */
public class Not<E> implements CustomSpecification<E> {

    private final Specification<E>[] specifications;

    /**
     * Конструктор.
     *
     * @param specifications спецификации
     */
    @SafeVarargs
    public Not(Specification<E>... specifications) {
        this.specifications = specifications;
    }

    /**
     * Возвращает спецификацию "НЕ".
     *
     * @return спецификация "НЕ"
     */
    @Override
    public Specification<E> specification() {
        if (specifications.length == 0) {
            return new Empty<>();
        }

        return (root, query, cb) -> {
            Predicate predicate = new And<>(specifications).toPredicate(root, query, cb);
            return predicate != null ? cb.not(predicate) : null;
        };
    }
}