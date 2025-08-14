package io.github.kovalev.datafetcher.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temporal_entity")
public class TemporalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_date")
    private LocalDate localDate;

    @Column(name = "local_time")
    private LocalTime localTime;

    @Column(name = "local_date_time")
    private LocalDateTime localDateTime;

    @Column(name = "instant")
    private Instant instant;

    @Column(name = "zoned_date_time")
    private ZonedDateTime zonedDateTime;

    @Column(name = "offset_date_time")
    private OffsetDateTime offsetDateTime;

    @Column(name = "util_date")
    private Date utilDate;

    @Column(name = "sql_date")
    private java.sql.Date sqlDate;

    @Column(name = "sql_time")
    private java.sql.Time sqlTime;

    @Column(name = "timestamp")
    private java.sql.Timestamp timestamp;
}
