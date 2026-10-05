package com.scaglione.coffeecappunipa.dto;

import com.scaglione.coffeecappunipa.entity.VendingMachine;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO per i distributori
 */
@Setter
@Getter
public class VendingMachineDto {

    /**
     * Identificativo univoco del distributore
     */
    private Long id;

    /**
     * Nome del distributore
     */
    private String name;

    /**
     * Posizione fisica del distributore
     */
    private String location;

    /**
     * Latitudine
     */
    private Double latitude;

    /**
     * Longitudine
     */
    private Double longitude;

    /**
     * Stato operativo corrente
     */
    private String status;

    /**
     * Livello acqua (0-100)
     */
    private Integer waterLevel;

    /**
     * Livello caffè (0-100)
     */
    private Integer coffeeLevel;

    /**
     * Livello latte (0-100)
     */
    private Integer milkLevel;

    /**
     * Livello zucchero (0-100)
     */
    private Integer sugarLevel;
    
    /**
     * ID dell'utente attualmente connesso se presente
     */
    private Long connectedUserId;

    /**
     * Username dell'utente connesso
     */
    private String username;

    /**
     * Credito residuo dell'utente connesso
     */
    private Double userCredit;

    /**
     * Messaggio di stato o errore da mostrare all'utente
     */
    private String message;


    public VendingMachineDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public VendingMachineDto(VendingMachine machine) {
        this.id = machine.getId();
        this.name = machine.getName();
        this.location = machine.getLocation();
        this.latitude = machine.getLatitude();
        this.longitude = machine.getLongitude();
        this.waterLevel = machine.getWaterLevel();
        this.coffeeLevel = machine.getCoffeeLevel();
        this.milkLevel = machine.getMilkLevel();
        this.sugarLevel = machine.getSugarLevel();
    }
}