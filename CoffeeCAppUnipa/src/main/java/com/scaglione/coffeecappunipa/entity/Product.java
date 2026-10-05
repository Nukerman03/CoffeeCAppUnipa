package com.scaglione.coffeecappunipa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Rappresenta la "ricetta" di una bevanda nel listino prezzi del sistema
 * Definisce il costo e il consumo di risorse necessario per l'erogazione
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    /**
     * Identificativo univoco del prodotto
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome univoco della bevanda (es. Espresso, Cappuccino)
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Prezzo di vendita al pubblico in Euro
     */
    @Column(nullable = false)
    private Double price;

    /**
     * Quantità di acqua consumata per singola erogazione
     */
    @Column(name = "water_req", nullable = false)
    private Integer waterReq;

    /**
     * Quantità di caffè consumata per singola erogazione
     */
    @Column(name = "coffee_req", nullable = false)
    private Integer coffeeReq;

    /**
     * Quantità di latte consumata per singola erogazione
     */
    @Column(name = "milk_req", nullable = false)
    private Integer milkReq;

    /**
     * Quantità base di zucchero consumata per singola erogazione
     * A questo valore si somma la scelta opzionale dell'utente
     */
    @Column(name = "sugar_req", nullable = false)
    private Integer sugarReq;

    public Product() {}
}