package com.scaglione.coffeecappunipa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entità che definisce i permessi e i privilegi degli utenti nel sistema
 * I ruoli principali sono USER, MANAGER e MAINTAINER
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificativo univoco del ruolo
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome del ruolo (es. ROLE_USER, ROLE_MANAGER)
     * Deve essere univoco nel sistema
     */
    @Column(unique = true, nullable = false)
    private String name;

    public Role() {}

}