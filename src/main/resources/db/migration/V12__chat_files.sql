create table if not exists project_files (
                                             id uuid primary key,
                                             project_id uuid not null,
                                             uploader_id uuid not null,
                                             original_name varchar(255) not null,
    stored_name varchar(255) not null,
    content_type varchar(128),
    size bigint not null,
    created_at timestamp not null
    );

create index if not exists idx_project_files_project on project_files(project_id);

create table if not exists project_message_attachments (
                                                           id uuid primary key,
                                                           message_id uuid not null,
                                                           file_id uuid not null,
                                                           created_at timestamp not null,
                                                           constraint uq_msg_file unique (message_id, file_id)
    );

create index if not exists idx_pma_message on project_message_attachments(message_id);
create index if not exists idx_pma_file on project_message_attachments(file_id);
