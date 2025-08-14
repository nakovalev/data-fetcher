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

class GreaterThanOrEqualToTest extends DatabaseTest {

    @Test
    void integerComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(10);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(9, ComparableEntity_.INT_VALUE));
        assertFound(new GreaterThanOrEqualTo<>(10, ComparableEntity_.INT_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>(11, ComparableEntity_.INT_VALUE));
    }

    @Test
    void bigDecimalComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setBigDecimalValue(new BigDecimal("15.7500"));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(new BigDecimal("15.7499"), ComparableEntity_.BIG_DECIMAL_VALUE));
        assertFound(new GreaterThanOrEqualTo<>(new BigDecimal("15.75"), ComparableEntity_.BIG_DECIMAL_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>(new BigDecimal("15.7501"), ComparableEntity_.BIG_DECIMAL_VALUE));
    }

    @Test
    void stringComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setStringValue("Hello");
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>("Hell", ComparableEntity_.STRING_VALUE));
        assertFound(new GreaterThanOrEqualTo<>("Hello", ComparableEntity_.STRING_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>("Hellz", ComparableEntity_.STRING_VALUE));
    }

    @Test
    void charComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setCharValue('M');
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>('L', ComparableEntity_.CHAR_VALUE));
        assertFound(new GreaterThanOrEqualTo<>('M', ComparableEntity_.CHAR_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>('N', ComparableEntity_.CHAR_VALUE));
    }

    @Test
    void dateComparison() {
        LocalDate today = LocalDate.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDateValue(today);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(today.minusDays(1), ComparableEntity_.DATE_VALUE));
        assertFound(new GreaterThanOrEqualTo<>(today, "dateValue"));
        assertNotFound(new GreaterThanOrEqualTo<>(today.plusDays(1), ComparableEntity_.DATE_VALUE));
    }

    @Test
    void datetimeComparison() {
        LocalDateTime now = LocalDateTime.now();
        ComparableEntity entity = new ComparableEntity();
        entity.setDatetimeValue(now);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(now.minusMinutes(1), ComparableEntity_.DATETIME_VALUE));
        assertFound(new GreaterThanOrEqualTo<>(now, ComparableEntity_.DATETIME_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>(now.plusMinutes(1), ComparableEntity_.DATETIME_VALUE));
    }

    @Test
    void multipleFieldsComparison() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(5);
        entity.setStringValue("Text");
        entity.setDateValue(LocalDate.of(2023, 6, 15));
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(4, ComparableEntity_.INT_VALUE));
        assertFound(new GreaterThanOrEqualTo<>("Tex", ComparableEntity_.STRING_VALUE));
        assertFound(new GreaterThanOrEqualTo<>(LocalDate.of(2023, 6, 14), ComparableEntity_.DATE_VALUE));
    }

    @Test
    void nullValueHandling() {
        ComparableEntity entity = new ComparableEntity();
        entity.setIntValue(1);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        assertFound(new GreaterThanOrEqualTo<>(null, ComparableEntity_.INT_VALUE));
        assertNotFound(new GreaterThanOrEqualTo<>(2L, ComparableEntity_.LONG_VALUE));
    }

    private void assertFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isPresent();
    }

    private void assertNotFound(Specification<ComparableEntity> spec) {
        assertThat(comparableDataFetcher.one(spec)).isEmpty();
    }
}