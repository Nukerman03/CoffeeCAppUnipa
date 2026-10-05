package com.scaglione.coffeecappunipa.service;

import com.scaglione.coffeecappunipa.dao.UserDao;
import com.scaglione.coffeecappunipa.dao.VendingMachineDao;
import com.scaglione.coffeecappunipa.dto.VendingMachineDto;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service che gestisce il polling e l'aggiornamento dello stato in tempo reale per l'interfaccia del distributore
 */
@Service
public class HeartbeatService {

    @Autowired
    private VendingMachineDao vendingMachineDao;
    @Autowired
    private UserDao userDao;

    /**
     * Elabora l'heartbeat proveniente dal frontend del distributore
     * Aggiorna il timestamp dell'ultimo contatto e restituisce lo stato corrente della macchina
     * Se un utente è connesso, popola il DTO con i suoi dati per aggiornare l'interfaccia
     *
     * @param machineId L'ID del distributore che sta inviando l'heartbeat
     * @return Un oggetto VendingMachineDto contenente lo stato aggiornato e i dati dell'utente connesso se presente
     */
    @Transactional
    public VendingMachineDto processHeartbeat(Long machineId) {
        VendingMachine machine = vendingMachineDao.findById(machineId).orElse(null);

        if (machine == null) {
            return new VendingMachineDto("ERROR", "Macchina non registrata");
        }

        machine.setLastHeartbeat(LocalDateTime.now());
        vendingMachineDao.save(machine);

        VendingMachineDto dto = new VendingMachineDto(machine);

        if (machine.getConnectedUserId() != null) {
            User user = userDao.findById(machine.getConnectedUserId()).orElse(null);

            if (user != null) {
                dto.setStatus("CONNECTED");
                dto.setConnectedUserId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setUserCredit(user.getCredit());
            } else {
                machine.setConnectedUserId(null);
                vendingMachineDao.save(machine);
                dto.setStatus("IDLE");
                dto.setMessage("Utente non trovato, reset connessione");
            }
        } else {
            dto.setStatus("IDLE");
            dto.setMessage("In attesa...");
        }

        return dto;
    }
}