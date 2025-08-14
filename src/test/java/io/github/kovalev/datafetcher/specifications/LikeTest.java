package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import io.github.kovalev.datafetcher.enums.WildcardPosition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeTest extends DatabaseTest {

    @Test
    void basicLikeSearch() {
        User user = userGenerator.one();
        user.setUsername("john_doe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Поиск с wildcards с обеих сторон (по умолчанию)
        assertThat(userDataFetcher.one(new Like<>("john", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("doe", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("hn_do", User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("unknown", User_.USERNAME))).isEmpty();
    }

    @Test
    void caseSensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-sensitive поиск (по умолчанию)
        assertThat(userDataFetcher.one(new Like<>("john", User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new Like<>("John", User_.USERNAME))).isPresent();
    }

    @Test
    void caseInsensitiveSearch() {
        User user = userGenerator.one();
        user.setUsername("JohnDoe");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Case-insensitive поиск
        assertThat(userDataFetcher.one(new Like<>("john", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("DOE", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("hNd", true, User_.USERNAME))).isPresent();
    }

    @Test
    void wildcardPositionVariations() {
        User user = userGenerator.one();
        user.setUsername("searchterm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // START_ONLY - ищем в конце строки
        assertThat(userDataFetcher.one(new Like<>("term", WildcardPosition.START_ONLY, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("search", WildcardPosition.START_ONLY, User_.USERNAME))).isEmpty();

        // END_ONLY - ищем в начале строки
        assertThat(userDataFetcher.one(new Like<>("search", WildcardPosition.END_ONLY, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("term", WildcardPosition.END_ONLY, User_.USERNAME))).isEmpty();

        // NONE - точное совпадение (эквивалент equals)
        assertThat(userDataFetcher.one(new Like<>("searchterm", WildcardPosition.NONE, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("search", WildcardPosition.NONE, User_.USERNAME))).isEmpty();
    }

    @Test
    void standardLikeBehavior() {
        User user = userGenerator.one();
        user.setUsername("john_doe");
        user.setEmail("100%_match@example.com");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // _ работает как single-character wildcard
        assertThat(userDataFetcher.one(new Like<>("j%_d%", User_.USERNAME))).isPresent();

        // Поиск реальных спецсимволов с экранированием
        assertThat(userDataFetcher.one(new Like<>("100\\%\\_match", User_.EMAIL))).isPresent();

        // Смешанный случай
        assertThat(userDataFetcher.one(new Like<>("10%\\_ma%", User_.EMAIL))).isPresent();
    }

    @Test
    void edgeCases() {
        User user = userGenerator.one();
        user.setUsername("\\_escape_test");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Поиск escape-символа
        assertThat(userDataFetcher.one(new Like<>("\\\\_esc%", User_.USERNAME))).isPresent();
    }

    @Test
    void nullValueHandling() {
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(userGenerator.one()));

        // При null значении - пустая спецификация
        assertThat(userDataFetcher.one(new Like<>(null, User_.USERNAME))).isPresent();
    }

    @Test
    void fullConstructorCombination() {
        User user = userGenerator.one();
        user.setUsername("SearchTerm");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Проверка всех вариантов конструктора
        assertThat(userDataFetcher.one(new Like<>("term", User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new Like<>("term", true, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("term", WildcardPosition.END_ONLY, User_.USERNAME))).isEmpty();
        assertThat(userDataFetcher.one(new Like<>("term", WildcardPosition.START_ONLY, true, User_.USERNAME))).isPresent();
    }

    @Test
    void exactMatchWithNoWildcards() {
        User user = userGenerator.one();
        user.setUsername("exactmatch");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        // Точное совпадение без wildcards
        assertThat(userDataFetcher.one(new Like<>("exactmatch", WildcardPosition.NONE, User_.USERNAME))).isPresent();
        assertThat(userDataFetcher.one(new Like<>("exact", WildcardPosition.NONE, User_.USERNAME))).isEmpty();
    }
}