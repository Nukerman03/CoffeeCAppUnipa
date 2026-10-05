package com.scaglione.coffeecappunipa.dao;

import com.scaglione.coffeecappunipa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DAO per l'entità User
 * Gestisce l'accesso ai dati degli utenti registrati nel sistema
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {

    /**
     * Recupera un utente univoco basato sul suo username
     *
     * @param username Lo username dell'utente
     * @return Un Optional contenente l'utente se trovato
     */
    Optional<User> findByUsername(String username);

    /**
     * Recupera una lista di utenti filtrati per nome del ruolo
     *
     * @param roleName Il nome del ruolo per cui filtrare (es. "ROLE_MAINTAINER")
     * @return Una lista di utenti che possiedono il ruolo specificato
     */
    List<User> findByRoleName(String roleName);
}