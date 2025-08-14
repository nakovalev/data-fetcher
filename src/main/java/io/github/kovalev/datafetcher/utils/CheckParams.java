package io.github.kovalev.datafetcher.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckParams implements NonNullParams {

  private final Object value;
  private final String[] fields;

  @Override
  public boolean nonNull() {
    return new CheckFields(fields).nonNull() && new CheckValue(value).nonNull();
  }
}
