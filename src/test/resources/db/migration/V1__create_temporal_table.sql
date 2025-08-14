CREATE TABLE temporal_entity
(
    id               BIGSERIAL PRIMARY KEY,
    local_date       DATE,
    local_time       TIME,
    local_date_time  TIMESTAMP,
    instant          TIMESTAMP,
    zoned_date_time  TIMESTAMP WITH TIME ZONE,
    offset_date_time TIMESTAMP WITH TIME ZONE,
    timestamp        TIMESTAMP,
    util_date        TIMESTAMP,
    sql_date         DATE,
    sql_time         TIME
);