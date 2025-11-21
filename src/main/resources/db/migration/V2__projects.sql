create table projects (
                          id uuid primary key default uuid_generate_v4(),
                          owner_id uuid not null references users(id) on delete cascade,
                          name varchar(255) not null,
                          description text,
                          created_at timestamp not null default now()
);

create index idx_projects_owner_id on projects(owner_id);
