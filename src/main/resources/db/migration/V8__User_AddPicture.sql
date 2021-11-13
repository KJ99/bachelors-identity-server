alter table users add column picture_id int default null;
alter table users add constraint FK_user_picture foreign key(picture_id) references uploaded_files(id);