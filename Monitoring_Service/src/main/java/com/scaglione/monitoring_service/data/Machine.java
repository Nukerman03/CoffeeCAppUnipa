package com.scaglione.monitoring_service.data;

import java.time.LocalDateTime;

/**
 * Rappresenta l'entità di una macchina distributrice nel sistema di monitoraggio.
 */
public class Machine {

    /**
     * Identificativo univoco della macchina
     */
    private String id;

    /**
     * Etichetta descrittiva della posizione della macchina.
     */
    private String locationLabel;

    /**
     * Latitudine geografica della macchina.
     */
    private Double latitude;

    /**
     * Longitudine geografica della macchina.
     */
    private Double longitude;

    /**
     * Stato corrente della macchina (es. ATTIVA, GUASTA).
     */
    private MachineStatus status;

    /**
     * Timestamp dell'ultimo segnale di vita (heartbeat) ricevuto dalla macchina.
     */
    private LocalDateTime lastHeartbeat;

    /**
     * Timestamp dell'ultimo cambiamento di stato della macchina.
     */
    private LocalDateTime lastStatusChange;

    public Machine() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocationLabel() { return locationLabel; }
    public void setLocationLabel(String locationLabel) { this.locationLabel = locationLabel; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public LocalDateTime getLastStatusChange() { return lastStatusChange; }
    public void setLastStatusChange(LocalDateTime lastStatusChange) { this.lastStatusChange = lastStatusChange; }
}