package com.scaglione.coffeecappunipa.service;

import com.scaglione.coffeecappunipa.entity.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Client HTTP per la comunicazione con il servizio di monitoraggio esterno
 * Gestisce l'invio di notifiche relative al ciclo di vita e allo stato dei distributori
 */
@Service
public class MonitoringService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String MONITORING_URL = "http://localhost:8081/monitoring/machines";

    /**
     * Notifica al servizio di monitoraggio l'aggiunta di un nuovo distributore
     * Invia una richiesta POST con i dettagli della macchina
     *
     * @param vm L'entità VendingMachine appena creata
     */
    public void notifyAddMachine(VendingMachine vm) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", String.valueOf(vm.getId()));
            payload.put("locationLabel", vm.getLocation());
            payload.put("latitude", vm.getLatitude());
            payload.put("longitude", vm.getLongitude());
            payload.put("status", vm.getStatus());

            restTemplate.postForObject(MONITORING_URL, payload, String.class);
        } catch (Exception e) {
            System.err.println("Errore notifica Monitoring (Add): " + e.getMessage());
        }
    }

    /**
     * Notifica al servizio di monitoraggio la rimozione di un distributore
     * Invia una richiesta DELETE identificando la macchina tramite la sua posizione
     *
     * @param location La posizione (locationLabel) del distributore da rimuovere
     */
    public void notifyRemoveMachine(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
            restTemplate.delete(MONITORING_URL + "?locationLabel=" + encodedLocation);
        } catch (Exception e) {
            System.err.println("Errore notifica Monitoring (Remove): " + e.getMessage());
        }
    }

    /**
     * Notifica al servizio di monitoraggio il cambio di stato di un distributore
     * Invia una richiesta PUT con la nuova azione/stato
     *
     * @param location La posizione (locationLabel) del distributore
     * @param newStatus Il nuovo stato del distributore (es. GUASTA, MANUTENZIONE)
     */
    public void notifyStatusChange(String location, String newStatus) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("locationLabel", location);
            payload.put("action", newStatus);

            restTemplate.put(MONITORING_URL, payload);
        } catch (Exception e) {
            System.err.println("Errore notifica Monitoring (Status): " + e.getMessage());
        }
    }
}