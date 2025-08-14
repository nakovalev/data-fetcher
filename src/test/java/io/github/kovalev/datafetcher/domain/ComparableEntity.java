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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comparable_entity")
public class ComparableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "int_value")
    private Integer intValue;

    @Column(name = "long_value")
    private Long longValue;

    @Column(name = "double_value")
    private Double doubleValue;

    @Column(name = "big_decimal")
    private BigDecimal bigDecimalValue;

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "char_value")
    private Character charValue;

    @Column(name = "date_value")
    private LocalDate dateValue;

    @Column(name = "datetime_value")
    private LocalDateTime datetimeValue;
}
