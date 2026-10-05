package com.scaglione.coffeecappunipa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Rappresenta un utente registrato nel sistema
 * Può essere un cliente finale, un manutentore o un gestore
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Identificativo univoco dell'utente
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username univoco utilizzato per il login
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Password dell'utente
     */
    @Column(nullable = false)
    private String password;

    /**
     * Saldo virtuale dell'utente in Euro
     * Utilizzato per acquistare prodotti dai distributori
     */
    @Column(nullable = false)
    private Double credit;

    /**
     * Ruolo associato all'utente che ne determina i privilegi di accesso
     * Relazione Many-to-One con l'entità Role
     */
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    public User() {
        this.credit = 0.0;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.credit = 0.0;
    }

}