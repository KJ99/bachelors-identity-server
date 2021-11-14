insert into uploaded_files (id, directory, file_name, original_file_name, media_type) values
    (1, 'dir', 'filename.jpg', 'or-filename.jpg', 'image/jpg'),
    (2, 'dir', 'filename.jpg', 'or-filename.jpg', 'image/jpg'),
    (3, 'dir', 'filename.jpg', 'or-filename.jpg', 'image/jpg');

insert into users (uid, email, username, first_name, last_name, password, salt, picture_id) values
    ('uid-active-1', 'activeuser1@fakemail', 'active-1', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 'salt-1', 3),
    ('uid-active-2', 'activeuser2@fakemail', 'active-2', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 'salt-2', null),
    ('uid-active-3', 'activeuser3@fakemail', 'active-3', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 'salt-3', null),
    ('uid-not-verified-1', 'notverified1@fakemail', 'not-verified-1', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 'salt-4', null),
    ('uid-not-verified-2', 'notverified2@fakemail', 'not-verified-2', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 'salt-5', null);
