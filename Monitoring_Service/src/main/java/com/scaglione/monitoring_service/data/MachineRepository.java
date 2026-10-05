package com.scaglione.monitoring_service.data;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente di accesso ai dati per l'entità Machine
 */
@ApplicationScoped
public class MachineRepository {

    @Resource(name = "jdbc/monitoringDB")
    private DataSource dataSource;

    /**
     * Salva una nuova macchina nel database.
     *
     * @param machine L'oggetto Machine da persistere
     * @throws SQLException Se si verifica un errore SQL durante l'inserimento
     */
    public void save(Machine machine) throws SQLException {
        String sql = "INSERT INTO machines (id, location_label, latitude, longitude, status, last_heartbeat, last_status_change) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machine.getId());
            ps.setString(2, machine.getLocationLabel());
            ps.setObject(3, machine.getLatitude());
            ps.setObject(4, machine.getLongitude());
            ps.setString(5, machine.getStatus().name());
            ps.setObject(6, machine.getLastHeartbeat());
            ps.setObject(7, machine.getLastStatusChange());
            ps.executeUpdate();
        }
    }

    /**
     * Cerca una macchina nel database tramite la sua etichetta di locazione.
     *
     * @param locationLabel L'etichetta univoca della location da cercare
     * @return L'oggetto Machine trovato, oppure null se non esiste
     * @throws SQLException Se si verifica un errore SQL durante la ricerca
     */
    public Machine findByLocationLabel(String locationLabel) throws SQLException {
        String sql = "SELECT * FROM machines WHERE location_label = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, locationLabel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMachine(rs);
                }
            }
        }
        return null;
    }

    /**
     * Aggiorna i dati di una macchina esistente nel database.
     *
     * @param machine L'oggetto Machine con i dati aggiornati
     * @throws SQLException Se si verifica un errore SQL durante l'aggiornamento
     */
    public void update(Machine machine) throws SQLException {
        String sql = "UPDATE machines SET location_label = ?, latitude = ?, longitude = ?, status = ?, last_heartbeat = ?, last_status_change = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machine.getLocationLabel());
            ps.setObject(2, machine.getLatitude());
            ps.setObject(3, machine.getLongitude());
            ps.setString(4, machine.getStatus().name());
            ps.setObject(5, machine.getLastHeartbeat());
            ps.setObject(6, machine.getLastStatusChange());
            ps.setString(7, machine.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Rimuove una macchina dal database.
     *
     * @param machine L'oggetto Machine da eliminare
     * @throws SQLException Se si verifica un errore SQL durante l'eliminazione
     */
    public void delete(Machine machine) throws SQLException {
        String sql = "DELETE FROM machines WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machine.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Recupera tutte le macchine presenti nel database.
     *
     * @return Una lista di oggetti Machine
     * @throws SQLException Se si verifica un errore SQL durante il recupero
     */
    public List<Machine> findAll() throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String sql = "SELECT * FROM machines";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                machines.add(mapRowToMachine(rs));
            }
        }
        return machines;
    }

    /**
     * Trova le macchine attive che non hanno inviato un heartbeat recente.
     *
     * @param limit Il timestamp limite oltre il quale l'heartbeat è considerato vecchio
     * @return Una lista di macchine attive con heartbeat scaduto o mancante
     * @throws SQLException Se si verifica un errore SQL durante la ricerca
     */
    public List<Machine> findActiveMachinesWithOldHeartbeat(LocalDateTime limit) throws SQLException {
        List<Machine> machines = new ArrayList<>();
        String sql = "SELECT * FROM machines WHERE status = ? AND (last_heartbeat < ? OR last_heartbeat IS NULL)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, MachineStatus.ATTIVA.name());
            ps.setObject(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(mapRowToMachine(rs));
                }
            }
        }
        return machines;
    }

    /**
     * Mappa una riga del ResultSet in un oggetto Machine.
     *
     * @param rs Il ResultSet posizionato sulla riga da mappare
     * @return Un oggetto Machine popolato con i dati della riga
     * @throws SQLException Se si verifica un errore durante la lettura del ResultSet
     */
    private Machine mapRowToMachine(ResultSet rs) throws SQLException {
        Machine machine = new Machine();
        machine.setId(rs.getString("id"));
        machine.setLocationLabel(rs.getString("location_label"));
        machine.setLatitude(rs.getObject("latitude", Double.class));
        machine.setLongitude(rs.getObject("longitude", Double.class));
        machine.setStatus(MachineStatus.valueOf(rs.getString("status")));
        machine.setLastHeartbeat(rs.getObject("last_heartbeat", LocalDateTime.class));
        machine.setLastStatusChange(rs.getObject("last_status_change", LocalDateTime.class));
        return machine;
    }
}