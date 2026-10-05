package com.scaglione.monitoring_service.business;

import com.scaglione.monitoring_service.data.Machine;
import com.scaglione.monitoring_service.data.MachineRepository;
import com.scaglione.monitoring_service.data.MachineStatus;
import com.scaglione.monitoring_service.dto.MachineDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Logica di business per la gestione delle macchine
 */
@ApplicationScoped
public class MachineService {

    @Inject
    private MachineRepository repository;

    private final Logger LOGGER = Logger.getLogger(MachineService.class.getName());

    private LocalDateTime lastFaultCheck = LocalDateTime.MIN;
    private final int CHECK_INTERVAL_SECONDS = 30;

    /**
     * Aggiunge una nuova macchina al sistema
     *
     * @param dto Oggetto DTO contenente i dati della nuova macchina
     * @throws Exception Se esiste già una macchina con la stessa location
     */
    public void addMachine(MachineDTO dto) throws Exception {
        if (repository.findByLocationLabel(dto.getLocationLabel()) != null) {
            throw new Exception("Macchina già esistente con location: " + dto.getLocationLabel());
        }

        Machine machine = new Machine();
        machine.setId(dto.getId());
        machine.setLocationLabel(dto.getLocationLabel());
        machine.setLatitude(dto.getLatitude());
        machine.setLongitude(dto.getLongitude());
        machine.setStatus(dto.getStatus() != null ? dto.getStatus() : MachineStatus.ATTIVA);
        machine.setLastStatusChange(LocalDateTime.now());
        machine.setLastHeartbeat(LocalDateTime.now());

        repository.save(machine);
        LOGGER.info("Aggiunta macchina: " + machine.getId());
    }

    /**
     * Rimuove una macchina dal sistema basandosi sulla sua location
     *
     * @param locationLabel Etichetta univoca della location della macchina da rimuovere
     * @throws SQLException Se si verifica un errore durante l'accesso al database
     */
    public void removeMachine(String locationLabel) throws SQLException {
        Machine machine = repository.findByLocationLabel(locationLabel);
        if (machine != null) {
            repository.delete(machine);
            LOGGER.info("Rimossa macchina con location: " + locationLabel);
        }
    }

    /**
     * Aggiorna lo stato di una macchina specifica
     *
     * @param locationLabel Etichetta univoca della location della macchina
     * @param status Nuovo stato da assegnare alla macchina
     * @throws Exception Se la macchina non viene trovata
     */
    public void updateStatus(String locationLabel, MachineStatus status) throws Exception {
        Machine machine = repository.findByLocationLabel(locationLabel);
        if (machine == null) throw new Exception("Macchina non trovata con location: " + locationLabel);

        machine.setStatus(status);
        machine.setLastStatusChange(LocalDateTime.now());
        repository.update(machine);
        LOGGER.info("Stato aggiornato per " + locationLabel + ": " + status);
    }

    /**
     * Registra un heartbeat ricevuto da una macchina, identificandola tramite la location
     * Aggiorna il timestamp dell'ultimo segnale di vita ricevuto
     *
     * @param locationLabel Etichetta univoca della location della macchina che ha inviato l'heartbeat
     * @throws Exception Se la macchina non viene trovata
     */
    public void recordHeartbeatByLocation(String locationLabel) throws Exception {
        Machine machine = repository.findByLocationLabel(locationLabel);
        if (machine == null) {
            throw new Exception("Macchina non trovata per heartbeat (location): " + locationLabel);
        }

        machine.setLastHeartbeat(LocalDateTime.now());

        if (machine.getStatus() == MachineStatus.GUASTA) {
            LOGGER.info("Macchina GUASTA " + locationLabel + " ha inviato heartbeat.");
        }

        repository.update(machine);
        LOGGER.fine("Heartbeat ricevuto da: " + locationLabel);
    }

    /**
     * Recupera la lista di tutte le macchine registrate.
     * Prima di restituire la lista, esegue un controllo sui guasti (checkForFaults)
     * per assicurarsi che lo stato delle macchine sia aggiornato.
     *
     * @return Lista di oggetti MachineDTO rappresentanti tutte le macchine
     * @throws SQLException Se si verifica un errore durante l'accesso al database
     */
    public synchronized List<MachineDTO> getAllMachines() throws SQLException {
        if (ChronoUnit.SECONDS.between(lastFaultCheck, LocalDateTime.now()) > CHECK_INTERVAL_SECONDS) {
            checkForFaults();
            lastFaultCheck = LocalDateTime.now();
        }

        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Applica il Guasto Automatico
     * Se una macchina attiva non invia heartbeat da più di 3 minuti viene marcata come GUASTA
     *
     * @throws SQLException Se si verifica un errore durante l'aggiornamento del database
     */
    private void checkForFaults() throws SQLException {
        LOGGER.info("Esecuzione controllo periodico guasti...");
        LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);
        List<Machine> brokenMachines = repository.findActiveMachinesWithOldHeartbeat(threeMinutesAgo);

        for (Machine m : brokenMachines) {
            m.setStatus(MachineStatus.GUASTA);
            m.setLastStatusChange(LocalDateTime.now());
            repository.update(m);
            LOGGER.warning("RILEVATO GUASTO AUTOMATICO: Macchina " + m.getId());
        }
    }

    private MachineDTO convertToDTO(Machine machine) {
        return new MachineDTO(
                machine.getId(),
                machine.getLocationLabel(),
                machine.getLatitude(),
                machine.getLongitude(),
                machine.getStatus(),
                machine.getLastHeartbeat()
        );
    }
}