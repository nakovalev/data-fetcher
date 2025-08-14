package io.github.kovalev.datafetcher.specifications;

import org.springframework.data.jpa.domain.Specification;

import static org.springframework.data.jpa.domain.Specification.where;


/**
 * Класс для создания пустой спецификации.
 *
 * @param <E> тип сущности
 */
public class Empty<E> implements CustomSpecification<E> {

  /**
   * Возвращает пустую спецификацию.
   *
   * @return пустая спецификация
   */
  @Override
  public Specification<E> specification() {
    return where(null);
  }
}