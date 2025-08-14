package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import io.github.kovalev.datafetcher.enums.NullPolicy;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EqualTest extends DatabaseTest {

    @Test
    void equalWithNonNullValues() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new Equal<>(user.getId(), User_.ID))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>(user.getUsername(), User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>(user.getEmail(), User_.EMAIL))).isPresent();

        assertThat(userDataFetcher.one(new Equal<>("nonexistent", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new Equal<>("TestUser", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("testuser", User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new Equal<>("TESTUSER", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseInsensitiveComparison() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new Equal<>("TestUser", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("testuser", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("TESTUSER", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("different", true, User_.USERNAME))).isEmpty();
    }

    @Test
    void ignoreNullValue() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));
        assertThat(userDataFetcher.one(new Equal<>(null, User_.ID))).isPresent();
    }

    @Test
    void addNullAsIsNull() {
        User userWithNullEmail = userGenerator.one();
        userWithNullEmail.setEmail(null);
        User userWithEmail = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(userWithNullEmail);
            entityManager.persist(userWithEmail);
        });

        val emailIsNull = new Equal<User>(null, NullPolicy.ADD_AS_NULL, User_.EMAIL);
        assertThat(userDataFetcher.list(emailIsNull)).hasSize(1).allMatch(u -> u.getEmail() == null);

        val usernameEqual = new Equal<User>("test", NullPolicy.ADD_AS_NULL, User_.USERNAME);
        assertThat(userDataFetcher.one(usernameEqual)).isEmpty();
    }

    @Test
    void nonStringValueComparison() {
        User user = userGenerator.one();
        LocalDateTime createdAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new Equal<>(createdAt, User_.CREATED_AT))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>(createdAt.plusSeconds(1), User_.CREATED_AT))).isEmpty();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("TestUser");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        assertThat(userDataFetcher.one(new Equal<>("TestUser", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("TestUser", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("TestUser", NullPolicy.IGNORE, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Equal<>("TestUser", NullPolicy.ADD_AS_NULL, true, User_.USERNAME))).isPresent();
    }
}