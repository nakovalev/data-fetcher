package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrTest extends DatabaseTest {

    @Test
    void emptyOrReturnsEmpty() {
        Or<User> or = new Or<>();
        assertThat(userDataFetcher.list(or)).isEmpty();
    }

    @Test
    void singleConditionWorks() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        Or<User> or = new Or<>(new Equal<>(user.getId(), User_.ID));
        assertThat(userDataFetcher.list(or)).hasSize(1).containsExactly(user);
    }

    @Test
    void returnsResultsMatchingAnyCondition() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        Or<User> or = new Or<>(
                new Equal<>(user1.getUsername(), User_.USERNAME),
                new Equal<>(user2.getUsername(), User_.USERNAME)
        );
        assertThat(userDataFetcher.list(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void returnsOnlyMatchingResults() {
        User matchingUser = userGenerator.one();
        User nonMatchingUser = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(matchingUser);
            entityManager.persist(nonMatchingUser);
        });

        Or<User> or = new Or<>(
                new Equal<>(matchingUser.getUsername(), User_.USERNAME),
                new Equal<>("nonexistent", User_.USERNAME)
        );
        assertThat(userDataFetcher.list(or)).hasSize(1).containsExactly(matchingUser);
    }

    @Test
    void worksWithDifferentSpecificationTypes() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();
        user2.setCreatedAt(LocalDateTime.now().minusDays(1));

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        Or<User> or = new Or<>(
                new Equal<>(user1.getUsername(), User_.USERNAME),
                new LessThanOrEqualTo<>(LocalDateTime.now(), User_.CREATED_AT)
        );
        assertThat(userDataFetcher.list(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void collectionConstructorWorks() {
        User user1 = userGenerator.one();
        User user2 = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user1);
            entityManager.persist(user2);
        });

        List<Specification<User>> specs = List.of(
                new Equal<>(user1.getUsername(), User_.USERNAME),
                new Equal<>(user2.getUsername(), User_.USERNAME)
        );

        Or<User> or = new Or<>(specs);
        assertThat(userDataFetcher.list(or)).hasSize(2).contains(user1, user2);
    }

    @Test
    void combinesWithAndCorrectly() {
        User user = userGenerator.one();
        User nonMatchingUser = userGenerator.one();

        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(nonMatchingUser);
        });

        Or<User> or = new Or<>(
                new Equal<>(user.getEmail(), User_.EMAIL),
                new Equal<>("alternative@example.com", User_.EMAIL)
        );

        And<User> and = new And<>(
                new Equal<>(user.getUsername(), User_.USERNAME),
                or
        );

        assertThat(userDataFetcher.list(and)).hasSize(1).containsExactly(user);
    }
}