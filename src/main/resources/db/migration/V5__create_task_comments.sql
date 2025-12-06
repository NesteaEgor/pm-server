create table task_comments (
                               id uuid primary key,
                               task_id uuid not null references tasks(id) on delete cascade,
                               author_id uuid not null references users(id) on delete cascade,
                               text varchar(5000) not null,
                               created_at timestampz not null default now()
);

create index idx_task_comments_task_id_created_at on task_comments(task_id, created_at);
