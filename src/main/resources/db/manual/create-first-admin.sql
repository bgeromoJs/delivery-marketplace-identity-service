-- Execute este script conectado ao banco identity_db, no schema public.
-- Substitua os três valores abaixo antes de executar.
-- Requer a extensão pgcrypto para gerar uma senha compatível com BCrypt.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO public.users (
    id,
    name,
    email,
    password,
    phone,
    role,
    tenant_id,
    created_at
)
VALUES (
    gen_random_uuid(),
    'Administrador da Plataforma',
    'SEU_EMAIL@EXEMPLO.COM',
    crypt('SENHA_TEMPORARIA_TROQUE_DEPOIS', gen_salt('bf', 10)),
    '+55 11 99999-9999',
    'ADMIN',
    NULL,
    CURRENT_TIMESTAMP
);

-- Confira o cadastro sem exibir a senha:
SELECT id, name, email, phone, role, created_at
FROM public.users
WHERE email = 'SEU_EMAIL@EXEMPLO.COM';
