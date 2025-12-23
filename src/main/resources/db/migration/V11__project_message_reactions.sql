create table project_message_reactions (
                                           id uuid primary key,
                                           message_id uuid not null,
                                           user_id uuid not null,
                                           emoji varchar(16) not null,
                                           created_at timestamptz not null default now(),
                                           constraint fk_reaction_message foreign key (message_id) references project_messages(id) on delete cascade,
                                           constraint uq_reaction unique (message_id, user_id, emoji)
);
