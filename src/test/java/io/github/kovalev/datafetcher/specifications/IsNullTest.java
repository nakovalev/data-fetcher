package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsNullTest extends DatabaseTest {

    @Test
    void whenNotDataThenOptionalIsEmpty() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        val usernameSpec = new IsNull<User>(User_.USERNAME);
        val emailSpec = new IsNull<User>(User_.EMAIL);
        val createdAtSpec = new IsNull<User>(User_.CREATED_AT);

        assertThat(userDataFetcher.one(usernameSpec)).isEmpty();
        assertThat(userDataFetcher.one(emailSpec)).isEmpty();
        assertThat(userDataFetcher.one(createdAtSpec)).isEmpty();
    }

    @Test
    void whenExistDataThenOptionalIsNotEmpty() {
        User user = userGenerator.one();
        user.setEmail(null);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        val emailSpec = new IsNull<User>(User_.EMAIL);
        assertThat(userDataFetcher.one(emailSpec)).isPresent();
    }
}