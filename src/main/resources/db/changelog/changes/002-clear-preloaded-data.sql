--liquibase formatted sql

--changeset delivery:identity-006-clear-preloaded-data
DELETE FROM public.password_reset_tokens;
DELETE FROM public.admin_audit_entries;
DELETE FROM public.users;
DELETE FROM public.platform_settings;
--rollback INSERT INTO public.platform_settings (id, payment_proposal_validity_hours, order_without_courier_alert_minutes, store_link_domain) VALUES (1, 24, 12, 'deliveryhub.com.br') ON CONFLICT (id) DO NOTHING;
