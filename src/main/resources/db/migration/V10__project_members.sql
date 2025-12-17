create table project_members (
                                 id uuid primary key,
                                 project_id uuid not null references projects(id) on delete cascade,
                                 user_id uuid not null references users(id) on delete cascade,
                                 role varchar(32) not null,
                                 created_at timestamp not null default now()
);

create unique index uq_project_members_project_user
    on project_members(project_id, user_id);

create index idx_project_members_user
    on project_members(user_id);

create index idx_project_members_project
    on project_members(project_id);
