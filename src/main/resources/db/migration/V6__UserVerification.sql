create table user_verifications (
    token varchar(255) not null primary key,
    pin varchar(6) not null,
    expires_at datetime not null,
    created_at datetime default current_timestamp,
    updated_at datetime default null,
    user_id varchar(255) not null,
    foreign key (user_id)
        references users(uid)
        on delete cascade
);