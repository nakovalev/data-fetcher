package io.github.kovalev.datafetcher;

public interface Search<T> extends Filter<T> {

    String query();
}
