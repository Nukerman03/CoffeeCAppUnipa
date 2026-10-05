package com.scaglione.coffeecappunipa.dao;

import com.scaglione.coffeecappunipa.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * DAO per l'entità Role
 * Gestisce l'accesso ai ruoli e ai permessi del sistema
 */
@Repository
public interface RoleDao extends JpaRepository<Role, Long> {

    /**
     * Recupera un ruolo univoco basato sul suo nome
     *
     * @param name Il nome del ruolo (es. "ROLE_USER", "ROLE_MAINTAINER")
     * @return Un Optional contenente il ruolo se trovato
     */
    Optional<Role> findByName(String name);
}