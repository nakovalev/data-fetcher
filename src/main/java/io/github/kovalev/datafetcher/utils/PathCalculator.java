package io.github.kovalev.datafetcher.utils;


import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.lang.NonNull;

import java.util.Optional;

public class PathCalculator<E, P> {

    private final Root<E> root;
    private final String[] fields;

    public PathCalculator(@NonNull Root<E> root, @NonNull String... fields) {
        this.root = root;
        this.fields = fields;
    }

    public Path<P> path() {
        if (fields.length == 1) {
            return root.get(fields[0]);
        }

        Join<?, ?> join = getOrCreateJoin(root, fields[0]);

        if (fields.length == 2) {
            return join.get(fields[1]);
        }

        for (int i = 1; i < fields.length - 1; i++) {
            join = getOrCreateJoin(join, fields[i]);
        }

        return join.get(fields[fields.length - 1]);
    }

    /*
     * rootOrJoin.getFetches - коллекция жадных JOIN. Таких, где fetchType.EAGER
     * rootOrJoin.getJoins - коллекция основных JOIN, добавленных в ручную при построении запроса
     * rootOrJoin.join - создает новый JOIN в коллекции joins
     */
    private <X> Join<X, ?> getOrCreateJoin(From<?, X> rootOrJoin, String attribute) {
        Optional<Fetch<X, ?>> fetch = rootOrJoin.getFetches().stream()
                .filter(f -> f.getAttribute().getName().equals(attribute) && f.getJoinType() == JoinType.LEFT)
                .findFirst();

        if (fetch.isPresent()) {
            return rootOrJoin.join(attribute, fetch.get().getJoinType());
        }

        Optional<Join<X, ?>> join = rootOrJoin.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals(attribute) && j.getJoinType() == JoinType.LEFT)
                .findFirst();

        return join.orElseGet(() -> rootOrJoin.join(attribute, JoinType.LEFT));
    }
}
