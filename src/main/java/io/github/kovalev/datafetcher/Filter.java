package io.github.kovalev.datafetcher;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Базовый класс фильтра.
 */
@FunctionalInterface
public interface Filter<T> {

  /**
   * Определение предиката для фильтра.
   *
   * @return предикат
   */
  default Predicate toPredicate(Root<T> root, CriteriaBuilder criteriaBuilder, CommonAbstractCriteria criteria) {
    return toSpecification().toPredicate(root, criteriaBuilder.createQuery(), criteriaBuilder);
  }

  /**
   * Определение спецификации для фильтра.
   *
   * @return спецификация
   */
  Specification<T> toSpecification();
}
