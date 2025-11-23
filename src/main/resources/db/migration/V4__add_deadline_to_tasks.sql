alter table tasks
    add column deadline timestamp null;

create index idx_tasks_deadline on tasks(deadline);
