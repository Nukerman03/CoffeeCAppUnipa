package com.scaglione.coffeecappunipa.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO per la gestione dei dati utente
 * Utilizzato sia per il trasferimento dati in input (Login, Registrazione)
 * sia per l'output verso la dashboard utente
 */
@Setter
@Getter
public class UserDto {

    /**
     * Username dell'utente
     */
    private String username;

    /**
     * Password dell'utente
     */
    private String password;

    /**
     * Credito dell'utente
     */
    private Double credit;

    /**
     * Nome formattato del distributore a cui l'utente è connesso
     */
    private String connectedMachineDisplay;

    /**
     * ID del distributore attualmente connesso
     * Mantiene il riferimento alla sessione attiva con una specifica macchina
     */
    private Long connectedMachineId;
}