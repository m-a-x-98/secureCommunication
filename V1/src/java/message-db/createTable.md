CREATE TABLE messages (
    id serial primary key,
    msg bytea not null,
    pad_start_offset bigint not null,
    usrname varchar(255) not null,
    recipient varchar(255) not null,
    send_at TIMESTAMP(3) NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);