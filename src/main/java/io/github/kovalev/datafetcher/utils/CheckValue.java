package io.github.kovalev.datafetcher.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
public class CheckValue implements NonNullParams {

  private final Object value;

  @Override
  public boolean nonNull() {
      return switch (value) {
          case null -> false;
          case String str -> StringUtils.hasText(str);
          case Collection<?> collection -> !collection.isEmpty() && collection.stream().anyMatch(Objects::nonNull);
          default -> true;
      };
  }
}
