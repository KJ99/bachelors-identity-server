create table users (
    uid varchar(255) not null,
    email varchar(128) not null,
    username varchar(128) not null,
    first_name varchar(128) not null,
    last_name varchar(128) not null,
    verified tinyint(1) not null default 0,
    active tinyint(1) not null default 0,
    created_at datetime default current_timestamp,
    updated_at datetime default null
);