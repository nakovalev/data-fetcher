CREATE TABLE comparable_entity
(
    id             BIGSERIAL PRIMARY KEY,
    int_value      INT,
    long_value     BIGINT,
    double_value   NUMERIC,
    big_decimal    DECIMAL(19, 4),
    string_value   VARCHAR(255),
    char_value     CHAR(1),
    date_value     DATE,
    datetime_value TIMESTAMP
);