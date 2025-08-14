package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsNotNullTest extends DatabaseTest {

    @Test
    void whenNullDataThenOptionalIsEmpty() {
        User user = userGenerator.one();
        user.setEmail(null);

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        val emailSpec = new IsNotNull<User>(User_.EMAIL);
        assertThat(userDataFetcher.one(emailSpec)).isEmpty();

        val notIsNullSpec = new Not<User>(new IsNull<>(User_.EMAIL));
        assertThat(userDataFetcher.one(emailSpec))
                .isEqualTo(userDataFetcher.one(notIsNullSpec));
    }

    @Test
    void whenNotNullDataThenOptionalIsNotEmpty() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        val emailSpec = new IsNotNull<User>(User_.EMAIL);
        assertThat(userDataFetcher.one(emailSpec)).isPresent();

        val notIsNullSpec = new Not<User>(new IsNull<>(User_.EMAIL));
        assertThat(userDataFetcher.one(emailSpec))
                .isEqualTo(userDataFetcher.one(notIsNullSpec));
    }
}