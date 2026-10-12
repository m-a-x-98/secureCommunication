CREATE TABLE keys (
    usrName      VARCHAR(255) UNIQUE NOT NULL,
    pwd_hash     TEXT NOT NULL,
    key_material BYTEA,
    pad_position BIGINT DEFAULT 0,
    pad_size     BIGINT
);