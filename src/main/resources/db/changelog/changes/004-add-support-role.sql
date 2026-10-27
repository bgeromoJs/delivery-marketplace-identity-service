--liquibase formatted sql

--changeset delivery:identity-008-allow-support-role
-- O arquivo foi regravado em UTF-8 sem acentos corrompidos, sem mudar o SQL.
-- Bancos que já aplicaram a versão anterior continuam válidos com o checksum abaixo.
--validCheckSum: 9:27a8374f2336bac42fc9265546da3468
-- A restricao original (criada fora do Liquibase em bases antigas) so aceitava
-- ADMIN, SELLER, COURIER e CUSTOMER. O perfil de analista de suporte passa a ser valido.
ALTER TABLE public.users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE public.users
    ADD CONSTRAINT users_role_check
    CHECK (role IN ('ADMIN', 'SUPPORT', 'SELLER', 'COURIER', 'CUSTOMER'));
--rollback ALTER TABLE public.users DROP CONSTRAINT IF EXISTS users_role_check;
--rollback ALTER TABLE public.users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'SELLER', 'COURIER', 'CUSTOMER'));
