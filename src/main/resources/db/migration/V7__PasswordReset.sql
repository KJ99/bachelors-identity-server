create table password_resets (
    token varchar(255) not null primary key,
    pin varchar(30) not null,
    expires_at datetime not null,
    user_id varchar(255) not null,
    created_at datetime default current_timestamp,
    updated_at datetime default null,

    foreign key (user_id) references users(uid)
);