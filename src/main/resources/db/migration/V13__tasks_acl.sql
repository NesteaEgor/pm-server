-- 1) creator_id (кто создал задачу)
alter table tasks add column if not exists creator_id uuid;

update tasks t
set creator_id = p.owner_id
    from projects p
where p.id = t.project_id
  and t.creator_id is null;

alter table tasks alter column creator_id set not null;

alter table tasks
    add constraint fk_tasks_creator
        foreign key (creator_id) references users(id) on delete cascade;

create index if not exists idx_tasks_creator on tasks(creator_id);

-- 2) assignee_id (ответственный)
alter table tasks add column if not exists assignee_id uuid null;

alter table tasks
    add constraint fk_tasks_assignee
        foreign key (assignee_id) references users(id) on delete set null;

create index if not exists idx_tasks_assignee on tasks(assignee_id);
