insert into uploaded_files (id, file_name, original_file_name, media_type) values
    (1, 'filename.jpg', 'or-filename.jpg', 'image/jpg'),
    (2, 'filename.jpg', 'or-filename.jpg', 'image/jpg'),
    (3, 'filename.jpg', 'or-filename.jpg', 'image/jpg');

insert into users (uid, email, username, first_name, last_name, password, picture_id, verified, active) values
    ('uid-active-1', 'activeuser1@fakemail', 'active-1', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', 3, 1, 1),
    ('uid-active-2', 'activeuser2@fakemail', 'active-2', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', null, 1, 1),
    ('uid-active-3', 'activeuser3@fakemail', 'active-3', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', null, 1, 1),
    ('uid-not-verified-1', 'notverified1@fakemail', 'not-verified-1', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', null, 0, 0),
    ('uid-not-verified-2', 'notverified2@fakemail', 'not-verified-2', 'John', 'Doe', '$2a$12$6X.w8yRJeeAAGWQe3x6cJOXgb7LLyzMmclyswP9lAcwCY1klKosEC', null, 0, 0);

insert into user_verifications (token, pin, expires_at, user_id) values
    ('active-token-1', '012345', '3021-12-12 23:59:59', 'uid-not-verified-1'),
    ('active-token-2', '012345', '3021-12-12 23:59:59', 'uid-not-verified-2'),
    ('expired-token-1', '012345', '2020-12-12 23:59:59', 'uid-not-verified-1'),
    ('used-token-1', '012345', '3021-12-12 23:59:59', 'uid-active-1');

insert into password_resets (token, pin, expires_at, user_id) values
    ('active-token-1', '012345', '3021-12-12 23:59:59', 'uid-active-1'),
    ('active-token-2', '012345', '3021-12-12 23:59:59', 'uid-active-2'),
    ('expired-token-1', '012345', '1021-12-12 23:59:59', 'uid-active-1');