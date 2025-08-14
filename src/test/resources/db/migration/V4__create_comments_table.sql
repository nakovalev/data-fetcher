CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    text       TEXT      NOT NULL,
    author_id  UUID      NOT NULL,
    post_id    UUID      NOT NULL,
    created_at TIMESTAMP NOT NULL,

    FOREIGN KEY (author_id) REFERENCES users (id),
    FOREIGN KEY (post_id) REFERENCES posts (id)
);