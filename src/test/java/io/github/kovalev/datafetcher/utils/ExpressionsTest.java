package io.github.kovalev.datafetcher.utils;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.TemporalEntity;
import io.github.kovalev.datafetcher.domain.TemporalEntity_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionsTest extends DatabaseTest {

    @Test
    void testLocalDate() {
        LocalDate localDate = LocalDate.now();
        TemporalEntity entity = new TemporalEntity();
        entity.setLocalDate(localDate);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.LOCAL_DATE, localDate);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getLocalDate()).isEqualTo(localDate);
    }

    @Test
    void testTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        TemporalEntity entity = new TemporalEntity();
        entity.setTimestamp(timestamp);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.TIMESTAMP, timestamp);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testLocalTime() {
        LocalTime localTime = LocalTime.now().truncatedTo(ChronoUnit.MILLIS);
        TemporalEntity entity = new TemporalEntity();
        entity.setLocalTime(localTime);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.LOCAL_TIME, localTime);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getLocalTime()).isEqualTo(localTime);
    }

    @Test
    void testLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        TemporalEntity entity = new TemporalEntity();
        entity.setLocalDateTime(localDateTime);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.LOCAL_DATE_TIME, localDateTime);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getLocalDateTime()).isEqualTo(localDateTime);
    }

    @Test
    void testInstant() {
        Instant instant = Instant.now().truncatedTo(ChronoUnit.MICROS);
        TemporalEntity entity = new TemporalEntity();
        entity.setInstant(instant);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.INSTANT, instant);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getInstant()).isEqualTo(instant);
    }

    @Test
    void testZonedDateTime() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MICROS);
        TemporalEntity entity = new TemporalEntity();
        entity.setZonedDateTime(zonedDateTime);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.ZONED_DATE_TIME, zonedDateTime);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getZonedDateTime()).isEqualTo(zonedDateTime);
    }

    @Test
    void testOffsetDateTime() {
        OffsetDateTime offsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        TemporalEntity entity = new TemporalEntity();
        entity.setOffsetDateTime(offsetDateTime);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.OFFSET_DATE_TIME, offsetDateTime);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();
        assertThat(result).isNotNull();
        assertThat(result.getOffsetDateTime()).isEqualTo(offsetDateTime);
    }

    @Test
    void testUtilDate() {
        Date utilDate = new Date(System.currentTimeMillis());
        TemporalEntity entity = new TemporalEntity();
        entity.setUtilDate(utilDate);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.UTIL_DATE, utilDate);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();

        assertThat(result).isNotNull();
        /*
         * Hibernate автоматически конвертирует java.util.Date в java.sql.Timestamp при сохранении.
         *
         * Особенности сравнения:
         * 1. Разные toString() форматы:
         *    - Date:   "Sun Jul 06 04:58:31 MSK 2025" (старый формат)
         *    - Timestamp: "2025-07-06 04:58:31.572" (ISO-подобный)
         *
         * 2. equals() между Date и Timestamp всегда false, даже для одинаковых дат
         *
         * 3. Реальные варианты сравнения:
         *    - По timestamp: getTime()
         *    - Через Instant: toInstant()
         *    - Форматированием в одинаковый строковый формат
         *
         * Для тестов рекомендуется использовать сравнение по миллисекундам.
         */
        assertThat(result.getUtilDate().getTime())
                .as("Compare by milliseconds since epoch")
                .isEqualTo(utilDate.getTime());

        assertThat(result.getUtilDate().toInstant())
                .as("Compare using Instant")
                .isEqualTo(utilDate.toInstant());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        assertThat(df.format(result.getUtilDate()))
                .as("Compare using formatted string")
                .isEqualTo(df.format(utilDate));
    }

    @Test
    void testSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        TemporalEntity entity = new TemporalEntity();
        entity.setSqlDate(sqlDate);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.SQL_DATE, sqlDate);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();

        assertThat(result).isNotNull();
        /*
         * При new java.sql.Date(System.currentTimeMillis()) мы получаем Дата+Время, например 2025-07-06T14:25:33.123
         * Однако в таблице это хранится только как дата (без времени)
         * и при преобразовании в объект получаем 2025-07-06T00:00:00.000,
         * поэтому нужно явно приводить к дате, вызвав .toLocalDate(), либо toString()
         */
        assertThat(result.getSqlDate().toLocalDate()).isEqualTo(sqlDate.toLocalDate());
        assertThat(result.getSqlDate()).hasToString(sqlDate.toString());
    }

    @Test
    void testSqlTime() {
        java.sql.Time sqlTime = new java.sql.Time(System.currentTimeMillis());
        TemporalEntity entity = new TemporalEntity();
        entity.setSqlTime(sqlTime);
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(entity));

        val query = temporalQuery(TemporalEntity_.SQL_TIME, sqlTime);

        TemporalEntity result = entityManager.createQuery(query).getSingleResult();

        assertThat(result).isNotNull();
        /*
         * При new java.sql.Time(System.currentTimeMillis()) мы получаем Дата+Время, например 2025-07-06T04:16:12.105
         * Однако в таблице это хранится как время
         * и при преобразовании в объект получаем 1970-01-01T04:16:12.105,
         * поэтому нужно явно приводить ко времени, вызвав .toLocalTime(), либо toString()
         */
        assertThat(result.getSqlTime().toLocalTime()).isEqualTo(sqlTime.toLocalTime());
        assertThat(result.getSqlTime()).hasToString(sqlTime.toString());
    }

    private <T> CriteriaQuery<TemporalEntity> temporalQuery(String field, T value) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createQuery(TemporalEntity.class);
        val root = query.from(TemporalEntity.class);

        Expression<?> expression = new Expressions().get(cb, root.get(field), value);
        Predicate predicate = cb.equal(expression, value);

        query.where(predicate);

        return query;
    }
}
