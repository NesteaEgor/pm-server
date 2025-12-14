alter table project_messages
    add column client_message_id varchar(64);

create index idx_project_messages_client_id
    on project_messages(project_id, author_id, client_message_id);

-- уникальность для идемпотентности (retries)
create unique index ux_project_messages_dedupe
    on project_messages(project_id, author_id, client_message_id)
    where client_message_id is not null;
