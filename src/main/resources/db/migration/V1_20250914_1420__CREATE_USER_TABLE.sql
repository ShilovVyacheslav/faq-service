CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    fullname   VARCHAR(128) NOT NULL,
    username   VARCHAR(32)  NOT NULL UNIQUE,
    email      VARCHAR(64)  NOT NULL UNIQUE,
    password   VARCHAR(256) NOT NULL,
    role       VARCHAR(8)   NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);