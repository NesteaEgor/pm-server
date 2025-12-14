alter table project_messages
    add column edited_at timestamp null;

alter table project_messages
    add column deleted_at timestamp null;

create index idx_project_messages_project_deleted_at
    on project_messages(project_id, deleted_at);
