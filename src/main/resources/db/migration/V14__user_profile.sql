-- статус (короткая фраза)
alter table users
    add column if not exists status varchar(160);

-- локальное хранение аватарки (чтобы не зависеть от внешних URL)
alter table users
    add column if not exists avatar_stored_name varchar(255);

alter table users
    add column if not exists avatar_content_type varchar(128);

alter table users
    add column if not exists avatar_size bigint;
