-- liquibase formatted sql

-- changeset yuri:1
CREATE TABLE if not exists notification_tasks
(
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    notification_date_time TIMESTAMP NOT NULL
);
