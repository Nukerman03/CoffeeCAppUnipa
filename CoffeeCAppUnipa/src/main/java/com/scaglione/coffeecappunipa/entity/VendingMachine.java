package com.scaglione.coffeecappunipa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Rappresenta il "gemello digitale" di un distributore automatico fisico
 * Mantiene lo stato, le scorte, la posizione e la connessione con gli utenti
 */
@Entity
@Table(name = "vending_machines")
@Getter
@Setter
public class VendingMachine {

    /**
     * Identificativo univoco del distributore
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome univoco del distributore (es. "Bar3")
     */
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Descrizione testuale della posizione fisica (es. "Edificio 6, Piano Terra")
     * Utilizzata come chiave univoca per la sincronizzazione con il sistema di monitoring
     */
    @Column(nullable = false)
    private String location;

    /**
     * Coordinata geografica latitudine
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Coordinata geografica longitudine
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Stato operativo corrente del distributore (es. "ATTIVA", "GUASTA", "MANUTENZIONE")
     */
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'ATTIVA'", nullable = false)
    private String status;

    /**
     * Incasso totale accumulato dal distributore
     */
    @Column(nullable = false)
    private Double cash;

    /**
     * Timestamp dell'ultimo segnale di vita ricevuto dal poller
     * Indica se la macchina è online e raggiungibile
     */
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    /**
     * ID dell'utente attualmente connesso al distributore
     * se valorizzato, impedisce ad altri utenti di connettersi
     */
    @Column(name = "connected")
    private Long connectedUserId;

    /**
     * Livello del serbatoio dell'acqua (percentuale 0-100%)
     */
    @Column(name = "water_level", nullable = false)
    private Integer waterLevel = 100;

    /**
     * Livello del serbatoio del caffè (percentuale 0-100%)
     */
    @Column(name = "coffee_level", nullable = false)
    private Integer coffeeLevel = 100;

    /**
     * Livello del serbatoio del latte (percentuale 0-100%)
     */
    @Column(name = "milk_level", nullable = false)
    private Integer milkLevel = 100;

    /**
     * Livello del serbatoio dello zucchero (percentuale 0-100%)
     */
    @Column(name = "sugar_level", nullable = false)
    private Integer sugarLevel = 100;

    public VendingMachine() {
        this.cash = 0.0;
        this.status = "ATTIVA";
    }

}