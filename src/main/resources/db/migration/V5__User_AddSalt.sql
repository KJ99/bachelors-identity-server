alter table users add column salt varchar(255) not null;
alter table users add constraint UN_salt unique(salt);
alter table users add constraint UN_username unique(username);
alter table users add constraint UN_email unique(email);
alter table users add constraint PK_users primary key(uid);