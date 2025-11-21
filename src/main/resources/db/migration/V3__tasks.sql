create table tasks (
                       id uuid primary key default uuid_generate_v4(),
                       project_id uuid not null references projects(id) on delete cascade,

                       title varchar(255) not null,
                       description text,
                       status varchar(32) not null default 'TODO',

                       created_at timestamp not null default now()
);

create index idx_tasks_project_id on tasks(project_id);
create index idx_tasks_status on tasks(status);
