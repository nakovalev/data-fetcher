CREATE TABLE orders
(
    id        BIGSERIAL PRIMARY KEY,
    customer  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);