--liquibase formatted sql

--changeset delivery:identity-007-add-user-active
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS active;
