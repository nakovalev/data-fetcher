package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import io.github.kovalev.datafetcher.enums.NullPolicy;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotEqualTest extends DatabaseTest {

    @Test
    void notEqualWithNonNullValues() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new NotEqual<>("nonexistent", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>(UUID.randomUUID(), User_.ID))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>(user.getEmail() + "x", User_.EMAIL))).isPresent();

        assertThat(userDataFetcher.one(new NotEqual<>(user.getUsername(), User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new NotEqual<>("testuser", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>("TESTUSER", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>("TestUser", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new NotEqual<>("testuser", true, User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new NotEqual<>("TESTUSER", true, User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new NotEqual<>("TestUser", true, User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new NotEqual<>("different", true, User_.USERNAME))).isPresent();
    }

    @Test
    void ignoreNullValue() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        assertThat(userDataFetcher.one(new NotEqual<>(null, User_.ID))).isPresent();
    }

    @Test
    void addNullAsIsNotNull() {
        User userWithNullEmail = userGenerator.one();
        userWithNullEmail.setEmail(null);

        User userWithEmail = userGenerator.one();
        userWithEmail.setEmail("test@example.com");

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(userWithNullEmail);
            entityManager.persist(userWithEmail);
        });

        val emailIsNotNull = new NotEqual<User>(null, NullPolicy.ADD_AS_NULL, User_.EMAIL);
        assertThat(userDataFetcher.list(emailIsNotNull))
                .hasSize(1)
                .allMatch(u -> u.getEmail() != null);

        val usernameNotEqual = new NotEqual<User>("test", NullPolicy.ADD_AS_NULL, User_.USERNAME);
        assertThat(userDataFetcher.one(usernameNotEqual)).isPresent();
    }


    @Test
    void nonStringValueNotEqual() {
        User user = userGenerator.one();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));


        assertThat(userDataFetcher.one(new NotEqual<>(createdAt.plusSeconds(1), User_.CREATED_AT))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>(createdAt, User_.CREATED_AT))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new NotEqual<>("testuser", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>("testuser", true, User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new NotEqual<>("testuser", NullPolicy.IGNORE, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new NotEqual<>("testuser", NullPolicy.ADD_AS_NULL, true, User_.USERNAME))).isEmpty();
    }
}