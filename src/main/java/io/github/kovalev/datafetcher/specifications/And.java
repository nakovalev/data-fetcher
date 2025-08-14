package io.github.kovalev.datafetcher.specifications;


import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Класс для создания спецификации "И".
 *
 * @param <E> тип сущности
 */
public class And<E> implements CustomSpecification<E> {

    private final List<Specification<E>> specifications = new ArrayList<>();

    /**
     * Конструктор.
     *
     * @param specifications спецификации
     */
    @SafeVarargs
    public And(Specification<E>... specifications) {
        this.specifications.addAll(asList(specifications));
    }

    /**
     * Конструктор.
     *
     * @param specifications спецификации
     */
    public And(Collection<Specification<E>> specifications) {
        this.specifications.addAll(specifications);
    }

    /**
     * Добавляет спецификацию.
     *
     * @param specification спецификация
     */
    public void add(Specification<E> specification) {
        specifications.add(specification);
    }

    /**
     * Возвращает спецификацию "И".
     *
     * @return спецификация "И"
     */
    @Override
    public Specification<E> specification() {
        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(Empty::new);
    }
}
