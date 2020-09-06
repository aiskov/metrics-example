USE service_db;

CREATE TABLE task
(
    id varchar(36) not null,
    title varchar(200) not null,
    tags varchar(200) null
);

CREATE UNIQUE INDEX task_id_uindex
    ON task (id);

ALTER TABLE task
    ADD CONSTRAINT task_pk
        PRIMARY KEY (id);

INSERT INTO service_db.task (id, title, tags) VALUES
    ('2384f927-5e2f-3998-8baa-c768616287f5', 'Second task', 'Test'),
    ('a5764857-ae35-34dc-8f25-a9c9e73aa898', 'First task is personal', 'Test,Personal');