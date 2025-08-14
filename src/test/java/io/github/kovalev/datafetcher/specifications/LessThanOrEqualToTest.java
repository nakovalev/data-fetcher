package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.ComparableEntity;
import io.github.kovalev.datafetcher.domain.ComparableEntity_;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LessThanOrEqualToTest extends DatabaseTest {

    @Test
    void integerComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>(9, ComparableEntity_.INT_VALUE));
        assertFound(new LessThanOrEqualTo<>(10, ComparableEntity_.INT_VALUE));
        assertFound(new LessThanOrEqualTo<>(11, ComparableEntity_.INT_VALUE));
    }

    @Test
    void bigDecimalComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.7500"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>(new BigDecimal("15.7499"), ComparableEntity_.BIG_DECIMAL_VALUE));
        assertFound(new LessThanOrEqualTo<>(new BigDecimal("15.75"), ComparableEntity_.BIG_DECIMAL_VALUE));
        assertFound(new LessThanOrEqualTo<>(new BigDecimal("15.7501"), ComparableEntity_.BIG_DECIMAL_VALUE));
    }

    @Test
    void stringComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Hello");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>("Hell", ComparableEntity_.STRING_VALUE));
        assertFound(new LessThanOrEqualTo<>("Hello", ComparableEntity_.STRING_VALUE));
        assertFound(new LessThanOrEqualTo<>("Hellz", ComparableEntity_.STRING_VALUE));
    }

    @Test
    void charComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setCharValue('M');
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>('L', ComparableEntity_.CHAR_VALUE));
        assertFound(new LessThanOrEqualTo<>('M', ComparableEntity_.CHAR_VALUE));
        assertFound(new LessThanOrEqualTo<>('N', ComparableEntity_.CHAR_VALUE));
    }

    @Test
    void dateComparison() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>(today.minusDays(1), ComparableEntity_.DATE_VALUE));
        assertFound(new LessThanOrEqualTo<>(today, ComparableEntity_.DATE_VALUE));
        assertFound(new LessThanOrEqualTo<>(today.plusDays(1), ComparableEntity_.DATE_VALUE));
    }

    @Test
    void datetimeComparison() {
        LocalDateTime now = LocalDateTime.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDatetimeValue(now);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>(now.minusMinutes(1), ComparableEntity_.DATETIME_VALUE));
        assertFound(new LessThanOrEqualTo<>(now, ComparableEntity_.DATETIME_VALUE));
        assertFound(new LessThanOrEqualTo<>(now.plusMinutes(1), ComparableEntity_.DATETIME_VALUE));
    }

    @Test
    void multipleFieldsComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        entity.setStringValue("Text");
        entity.setDateValue(LocalDate.of(2023, 6, 15));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertNotFound(new LessThanOrEqualTo<>(4, ComparableEntity_.INT_VALUE));
        assertFound(new LessThanOrEqualTo<>(5, ComparableEntity_.INT_VALUE));
        assertFound(new LessThanOrEqualTo<>(6, ComparableEntity_.INT_VALUE));

        assertNotFound(new LessThanOrEqualTo<>("Te", ComparableEntity_.STRING_VALUE));
        assertFound(new LessThanOrEqualTo<>("Text", ComparableEntity_.STRING_VALUE));
        assertFound(new LessThanOrEqualTo<>("Texu", ComparableEntity_.STRING_VALUE));
    }

    @Test
    void nullValueHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(1);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new LessThanOrEqualTo<>(null, ComparableEntity_.INT_VALUE));
        assertFound(new LessThanOrEqualTo<>(2, ComparableEntity_.INT_VALUE));
        assertNotFound(new LessThanOrEqualTo<>(0L, ComparableEntity_.LONG_VALUE));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isEmpty();
    }
}