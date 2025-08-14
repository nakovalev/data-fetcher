package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.ComparableEntity;
import io.github.kovalev.datafetcher.domain.ComparableEntity_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BetweenTest extends DatabaseTest {

    @Test
    void integerBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // В пределах диапазона
        assertFound(new Between<>(List.of(4, 6), ComparableEntity_.INT_VALUE));
        // Граничные значения
        assertFound(new Between<>(List.of(5, 5), ComparableEntity_.INT_VALUE));
        assertFound(new Between<>(List.of(4, 5), ComparableEntity_.INT_VALUE));
        assertFound(new Between<>(List.of(5, 6), ComparableEntity_.INT_VALUE));
        // За пределами диапазона
        assertNotFound(new Between<>(List.of(6, 8), ComparableEntity_.INT_VALUE));
        assertNotFound(new Between<>(List.of(1, 4), ComparableEntity_.INT_VALUE));
    }

    @Test
    void bigDecimalBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.50"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new Between<>(List.of(new BigDecimal("15.00"), new BigDecimal("16.00")),
                ComparableEntity_.BIG_DECIMAL_VALUE));
        assertFound(new Between<>(List.of(new BigDecimal("15.50"), new BigDecimal("15.50")),
                ComparableEntity_.BIG_DECIMAL_VALUE));
        assertNotFound(new Between<>(List.of(new BigDecimal("16.00"), new BigDecimal("17.00")),
                ComparableEntity_.BIG_DECIMAL_VALUE));
    }

    @Test
    void stringBetween() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Mango");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Лексикографическое сравнение
        assertFound(new Between<>(List.of("Apple", "Orange"), ComparableEntity_.STRING_VALUE));
        assertFound(new Between<>(List.of("Mango", "Mango"), ComparableEntity_.STRING_VALUE));
        assertNotFound(new Between<>(List.of("Aardvark", "Lemon"), ComparableEntity_.STRING_VALUE));
    }

    @Test
    void dateBetween() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new Between<>(List.of(today.minusDays(1), today.plusDays(1)),
                ComparableEntity_.DATE_VALUE));
        assertFound(new Between<>(List.of(today, today), ComparableEntity_.DATE_VALUE));
        assertNotFound(new Between<>(List.of(today.plusDays(1), today.plusDays(2)),
                ComparableEntity_.DATE_VALUE));
    }

    @Test
    void partialNullRanges() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Только нижняя граница
        assertFound(new Between<>(Arrays.asList(9, null), ComparableEntity_.INT_VALUE));
        // Только верхняя граница
        assertFound(new Between<>(Arrays.asList(null, 11), ComparableEntity_.INT_VALUE));
        // Обе границы null
        List<Integer> nullBounds = Arrays.asList(null, null);
        assertFound(new Between<>(nullBounds, ComparableEntity_.INT_VALUE));
    }

    @Test
    void nullFieldHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(null); // поле null
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Для null-поля between не должен находить запись
        assertNotFound(new Between<>(List.of(1, 10), ComparableEntity_.INT_VALUE));
    }

    @Test
    void invalidParameters() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        // Слишком мало значений
        assertFound(new Between<>(new ArrayList<Integer>(), ComparableEntity_.INT_VALUE));
        // Все значения null
        List<Integer> nullBounds = Arrays.asList(null, null);
        assertFound(new Between<>(nullBounds, ComparableEntity_.INT_VALUE));
        // Неправильный порядок границ
        assertNotFound(new Between<>(List.of(6, 4), ComparableEntity_.INT_VALUE));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isEmpty();
    }
}