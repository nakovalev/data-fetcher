CREATE TABLE posts
(
    id         UUID PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    content    TEXT,
    author_id  UUID         NOT NULL,
    created_at TIMESTAMP    NOT NULL,

    FOREIGN KEY (author_id) REFERENCES users (id)
);