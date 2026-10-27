--liquibase formatted sql

--changeset delivery:identity-001-create-users
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    tenant_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_users_email UNIQUE (email)
);
--rollback DROP TABLE public.users;

--changeset delivery:identity-002-create-platform-settings
CREATE TABLE IF NOT EXISTS public.platform_settings (
    id BIGINT PRIMARY KEY NOT NULL,
    payment_proposal_validity_hours INTEGER NOT NULL,
    order_without_courier_alert_minutes INTEGER NOT NULL,
    store_link_domain VARCHAR(255) NOT NULL
);
--rollback DROP TABLE public.platform_settings;

--changeset delivery:identity-003-seed-platform-settings
INSERT INTO public.platform_settings (
    id,
    payment_proposal_validity_hours,
    order_without_courier_alert_minutes,
    store_link_domain
) VALUES (1, 24, 12, 'deliveryhub.com.br')
ON CONFLICT (id) DO NOTHING;
--rollback DELETE FROM public.platform_settings WHERE id = 1;

--changeset delivery:identity-004-create-password-reset-tokens
CREATE TABLE IF NOT EXISTS public.password_reset_tokens (
    id UUID PRIMARY KEY NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    attempts_remaining INTEGER NOT NULL,
    CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash)
);
--rollback DROP TABLE public.password_reset_tokens;

--changeset delivery:identity-005-create-admin-audit-entries
CREATE TABLE IF NOT EXISTS public.admin_audit_entries (
    id UUID PRIMARY KEY NOT NULL,
    actor_user_id UUID NOT NULL,
    actor_email VARCHAR(255) NOT NULL,
    action VARCHAR(80) NOT NULL,
    target VARCHAR(180) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
--rollback DROP TABLE public.admin_audit_entries;
