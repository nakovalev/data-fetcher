package io.github.kovalev.datafetcher.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class FetchParams<E, D> {

  private final Specification<E> specification;
  private final Pageable pageable;
  private final Function<List<Object>, D> mapper;
  private final List<List<String>> fields;

  public List<List<String>> fields() {
    return fields;
  }

  public D map(List<Object> map) {
    return mapper.apply(map);
  }

  public Pageable pageable() {
    return pageable;
  }

  public Specification<E> specification() {
    return specification;
  }
}
