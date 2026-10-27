package com.delivery.identity.model;

public enum Role {
    /** Administrador do sistema: acesso total ao painel e à gestão de usuários. */
    ADMIN,
    /** Analista de suporte: acesso operacional ao painel, sem gestão de usuários/configurações. */
    SUPPORT,
    SELLER,
    COURIER,
    CUSTOMER
}
