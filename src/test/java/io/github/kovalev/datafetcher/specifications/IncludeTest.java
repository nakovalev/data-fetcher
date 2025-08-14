package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class IncludeTest extends DatabaseTest {

    @Test
    void whenDataContainsThenNotEmptyList() {
        List<UUID> userUuids = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(user -> entityManager.persist(user))
                        .map(User::getId)
                        .toList()
                        .subList(0, 3));

        Include<User, UUID> uuidSpec = new Include<>(userUuids, User_.ID);
        List<User> result = userDataFetcher.list(uuidSpec);

        assertThat(result).hasSize(3);
    }

    @Test
    void whenDataDoesNotContainsThenNotEmptyList() {
        transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).forEach(user -> entityManager.persist(user)));

        List<UUID> uuids = IntStream.range(0, 5).mapToObj(i -> UUID.randomUUID()).toList();

        Include<User, UUID> uuidSpec = new Include<>(uuids, User_.ID);
        List<User> result = userDataFetcher.list(uuidSpec);

        assertThat(result).isEmpty();
    }

    @Test
    void whenParamsIsNotNullWhenFindAll() {
        List<UUID> userUuids = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(user -> entityManager.persist(user))
                        .map(User::getId)
                        .toList());

        Include<User, UUID> emptySpec1 = new Include<>(null, User_.ID);
        Include<User, UUID> emptySpec2 = new Include<>(List.of(UUID.randomUUID()));

        assertThat(userDataFetcher.list(emptySpec1)).hasSize(userUuids.size());
        assertThat(userDataFetcher.list(emptySpec2)).hasSize(userUuids.size());
    }
}