--liquibase formatted sql

--changeset delivery:identity-010-customer-stores
-- Lojas que o cliente abriu pelo link. A loja entra na lista inicial do app.
CREATE TABLE IF NOT EXISTS public.customer_stores (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL,
    slug VARCHAR(120) NOT NULL,
    tenant_id UUID,
    name VARCHAR(160),
    logo_url VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_customer_stores_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_customer_stores_user_slug
    ON public.customer_stores (user_id, slug);

CREATE INDEX IF NOT EXISTS idx_customer_stores_user
    ON public.customer_stores (user_id);

CREATE INDEX IF NOT EXISTS idx_customer_stores_tenant
    ON public.customer_stores (tenant_id);

--rollback DROP TABLE IF EXISTS public.customer_stores;
