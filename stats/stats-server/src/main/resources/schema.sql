DROP TABLE IF EXISTS stats;

CREATE TABLE IF NOT EXISTS stats
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app     VARCHAR(255)                NOT NULL,
    ip      VARCHAR(15)                 NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    uri     VARCHAR(255)                NOT NULL
)