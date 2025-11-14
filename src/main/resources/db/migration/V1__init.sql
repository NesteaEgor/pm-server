create extension if not exists "uuid-ossp";

create table users (
                       id uuid primary key default uuid_generate_v4(),
                       email varchar(255) not null unique,
                       password_hash varchar(255) not null,
                       display_name varchar(255) not null,
                       avatar_url varchar(1024),
                       created_at timestamp not null default now()
);
