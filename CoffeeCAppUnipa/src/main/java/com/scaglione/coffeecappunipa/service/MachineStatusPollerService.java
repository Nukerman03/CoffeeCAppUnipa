package com.scaglione.coffeecappunipa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Servizio di background per la sincronizzazione periodica degli stati delle macchine
 */
@Component
public class MachineStatusPollerService {

    @Autowired
    private VendingMachineService vendingMachineService;

    @Autowired
    private RestTemplate restTemplate;

    private static final String MONITORING_URL = "http://localhost:8081/monitoring/machines";

    /**
     * Sincronizza lo stato locale dei distributori con quello remoto del servizio di monitoraggio
     * Questo task viene eseguito automaticamente ogni 30 secondi
     * Recupera la lista completa delle macchine dal servizio esterno e aggiorna lo stato nel DB locale
     * se vengono rilevate discrepanze (guasti rilevati automaticamente dal monitoraggio)
     */
    @Scheduled(fixedRate = 30000)
    public void syncMachineStatuses() {
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    MONITORING_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            List<Map<String, Object>> machines = response.getBody();

            if (machines != null) {
                for (Map<String, Object> machineData : machines) {
                    String locationLabel = (String) machineData.get("locationLabel");
                    String status = (String) machineData.get("status");

                    if (locationLabel != null && status != null) {
                        vendingMachineService.updateMachineStatusByLocation(locationLabel, status);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante il polling dello stato macchine: " + e.getMessage());
        }
    }
}