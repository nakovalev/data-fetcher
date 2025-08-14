package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyTest extends DatabaseTest {

    @Test
    void returnAllEntities() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() -> userGenerator.list(10).stream()
                .peek(user -> entityManager.persist(user))
                .toList());

        Empty<User> emptySpec = new Empty<>();

        assertThat(userDataFetcher.list(emptySpec)).hasSize(users.size());
    }
}