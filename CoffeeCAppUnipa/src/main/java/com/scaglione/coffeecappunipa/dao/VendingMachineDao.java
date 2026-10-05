package com.scaglione.coffeecappunipa.dao;

import com.scaglione.coffeecappunipa.entity.VendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DAO per l'entità VendingMachine
 * Gestisce l'accesso ai dati dei distributori automatici
 */
@Repository
public interface VendingMachineDao extends JpaRepository<VendingMachine, Long> {

    /**
     * Recupera un distributore univoco basato sul suo nome
     *
     * @param name Il nome del distributore
     * @return Un Optional contenente il distributore se trovato
     */
    Optional<VendingMachine> findByName(String name);

    /**
     * Recupera un distributore univoco basato sulla sua posizione (location)
     *
     * @param location La stringa che descrive la posizione del distributore
     * @return Un Optional contenente il distributore se trovato
     */
    Optional<VendingMachine> findByLocation(String location);
}