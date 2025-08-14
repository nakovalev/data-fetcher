CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(255),
    email      VARCHAR(255),
    created_at TIMESTAMP
);