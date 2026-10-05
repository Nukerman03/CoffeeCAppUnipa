package com.scaglione.monitoring_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scaglione.monitoring_service.data.MachineStatus;
import java.time.LocalDateTime;

/**
 * DTO per il trasferimento dei dati delle macchine
 */
public class MachineDTO {
    private String id;

    /**
     * Etichetta della posizione
     */
    private String locationLabel;
    private Double latitude;
    private Double longitude;
    private MachineStatus status;

    /**
     * Timestamp dell'ultimo heartbeat
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastHeartbeat;
    private String action;

    public MachineDTO() {}

    public MachineDTO(String id, String locationLabel, Double latitude, Double longitude, MachineStatus status, LocalDateTime lastHeartbeat) {
        this.id = id;
        this.locationLabel = locationLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.lastHeartbeat = lastHeartbeat;
    }

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

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}