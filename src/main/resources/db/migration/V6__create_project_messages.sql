create table project_messages (
                                  id uuid primary key,
                                  project_id uuid not null references projects(id) on delete cascade,
                                  author_id uuid not null references users(id) on delete cascade,
                                  text varchar(5000) not null,
                                  created_at timestamp not null default now()
);

create index idx_project_messages_project_created_at
    on project_messages(project_id, created_at desc);
