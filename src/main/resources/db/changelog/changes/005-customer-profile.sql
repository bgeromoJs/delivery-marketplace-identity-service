--liquibase formatted sql

--changeset delivery:identity-009-customer-profile
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS google_sub VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS food_preferences VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_google_sub
    ON public.users (google_sub)
    WHERE google_sub IS NOT NULL;

CREATE TABLE IF NOT EXISTS public.customer_addresses (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL,
    label VARCHAR(40) NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    district VARCHAR(120),
    city VARCHAR(120) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zip_code VARCHAR(9),
    reference VARCHAR(255),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_customer_addresses_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_customer_addresses_user
    ON public.customer_addresses (user_id);

CREATE TABLE IF NOT EXISTS public.customer_cards (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL,
    brand VARCHAR(30) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    holder_name VARCHAR(120) NOT NULL,
    expiry_month INTEGER NOT NULL,
    expiry_year INTEGER NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_customer_cards_user FOREIGN KEY (user_id)
        REFERENCES public.users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_customer_cards_user
    ON public.customer_cards (user_id);

--rollback DROP TABLE IF EXISTS public.customer_cards;
--rollback DROP TABLE IF EXISTS public.customer_addresses;
--rollback DROP INDEX IF EXISTS public.uk_users_google_sub;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS food_preferences;
--rollback ALTER TABLE public.users DROP COLUMN IF EXISTS google_sub;
