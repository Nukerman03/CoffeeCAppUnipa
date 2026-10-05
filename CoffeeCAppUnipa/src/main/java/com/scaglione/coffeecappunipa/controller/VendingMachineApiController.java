package com.scaglione.coffeecappunipa.controller;

import com.scaglione.coffeecappunipa.dto.VendingMachineDto;
import com.scaglione.coffeecappunipa.service.HeartbeatService;
import com.scaglione.coffeecappunipa.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST che gestisce le chiamate API per i distributori automatici
 * Fornisce endpoint per le interazioni AJAX dal frontend (es. distributore.js)
 */
@RestController
@RequestMapping("/api/machine")
public class VendingMachineApiController {

    @Autowired
    private VendingMachineService vendingMachineService;
    @Autowired
    private HeartbeatService heartbeatService;

    /**
     * Endpoint di polling chiamato ciclicamente dal frontend del distributore
     * Delega al HeartbeatService il compito di processare la richiesta e restituisce
     * lo stato aggiornato della macchina (es. se un utente si è connesso)
     *
     * @param machineId L'ID del distributore
     * @return ResponseEntity contenente un DTO con lo stato aggiornato
     */
    @GetMapping("/{machineId}/poll")
    public ResponseEntity<VendingMachineDto> pollMachine(@PathVariable Long machineId) {
        VendingMachineDto status = heartbeatService.processHeartbeat(machineId);
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint per l'acquisto di un prodotto dal distributore
     * Riceve il nome del prodotto e il livello di zucchero, esegue la transazione
     * e restituisce un messaggio di successo o errore
     *
     * @param machineId L'ID del distributore
     * @param payload Un JSON contenente "product" (String) e "sugar" (int)
     * @return ResponseEntity con un messaggio di esito
     */
    @PostMapping("/{machineId}/buy")
    public ResponseEntity<?> buyProduct(@PathVariable Long machineId, @RequestBody Map<String, Object> payload) {
        try {
            String product = (String) payload.get("product");
            int sugar = Integer.parseInt(payload.get("sugar").toString());

            vendingMachineService.buyFromMachine(machineId, product, sugar);
            return ResponseEntity.ok("Acquisto completato con successo");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}