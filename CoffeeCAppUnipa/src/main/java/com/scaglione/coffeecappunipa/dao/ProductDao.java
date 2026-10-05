package com.scaglione.coffeecappunipa.dao;

import com.scaglione.coffeecappunipa.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DAO per l'entità Product
 * Gestisce l'accesso al listino prezzi e alle ricette delle bevande
 */
@Repository
public interface ProductDao extends JpaRepository<Product, Long> {

    /**
     * Recupera un prodotto univoco basato sul suo nome
     * Utilizzato per trovare la ricetta e il prezzo di una bevanda specifica
     *
     * @param name Il nome della bevanda
     * @return Un Optional contenente il prodotto se trovato
     */
    Optional<Product> findByName(String name);
}