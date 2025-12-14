create table project_chat_reads (
                                    id uuid primary key,
                                    project_id uuid not null references projects(id) on delete cascade,
                                    user_id uuid not null references users(id) on delete cascade,
                                    last_read_message_id uuid references project_messages(id) on delete set null,
                                    last_read_at timestamp not null default now(),
                                    updated_at timestamp not null default now(),
                                    constraint uq_project_chat_reads unique(project_id, user_id)
);

create index idx_project_chat_reads_project on project_chat_reads(project_id);
create index idx_project_chat_reads_user on project_chat_reads(user_id);
