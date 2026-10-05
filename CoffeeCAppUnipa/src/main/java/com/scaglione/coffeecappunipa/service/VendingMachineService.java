package com.scaglione.coffeecappunipa.service;

import com.scaglione.coffeecappunipa.dao.ProductDao;
import com.scaglione.coffeecappunipa.dao.UserDao;
import com.scaglione.coffeecappunipa.dao.VendingMachineDao;
import com.scaglione.coffeecappunipa.entity.Product;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.exception.InsufficientCreditException;
import com.scaglione.coffeecappunipa.exception.MachineNotFoundException;
import com.scaglione.coffeecappunipa.exception.MachineUnavailableException;
import com.scaglione.coffeecappunipa.exception.UserNotConnectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service per la gestione dei distributori automatici
 * Gestisce connessioni, acquisti, ripristino scorte, aggiornamenti di stato
 */
@Service
public class VendingMachineService {

    @Autowired
    private VendingMachineDao vendingMachineDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MonitoringService monitoringService;
    @Autowired
    private ProductDao productDao;

    /**
     * Trova un distributore tramite ID
     *
     * @param machineId L'ID del distributore
     * @return L'entità VendingMachine o null se non trovata
     */
    public VendingMachine findMachineById(Long machineId) {
        return vendingMachineDao.findById(machineId).orElse(null);
    }

    /**
     * Trova un distributore tramite input stringa (ID o Nome)
     *
     * @param input L'ID o il nome del distributore
     * @return L'entità VendingMachine o null se non trovata
     */
    public VendingMachine findMachineByInput(String input) {
        return vendingMachineDao.findByName(input).orElse(null);
    }

    /**
     * Recupera la lista di tutti i distributori registrati
     *
     * @return Lista di VendingMachine
     */
    public List<VendingMachine> getAllMachines() {
        return vendingMachineDao.findAll();
    }

    /**
     * Recupera la lista di tutti i prodotti disponibili
     *
     * @return Lista di Product
     */
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    /**
     * Crea un nuovo distributore e notifica il sistema di monitoring
     *
     * @param machine La nuova macchina da salvare
     * @throws RuntimeException Se esiste già una macchina con lo stesso nome
     */
    @Transactional
    public void createMachine(VendingMachine machine) {
        try {
            vendingMachineDao.save(machine);
            monitoringService.notifyAddMachine(machine);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Nome distributore già in uso");
        }
    }

    /**
     * Elimina un distributore e notifica il sistema di monitoring
     *
     * @param id L'ID del distributore da eliminare
     * @throws RuntimeException Se la macchina non può essere eliminata
     */
    @Transactional
    public void deleteMachine(Long id) {
        VendingMachine machine = vendingMachineDao.findById(id).orElse(null);
        String location = (machine != null) ? machine.getLocation() : null;

        try {
            vendingMachineDao.deleteById(id);
            if (location != null) {
                monitoringService.notifyRemoveMachine(location);
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossibile eliminare: elemento in uso o inesistente");
        }
    }

    /**
     * Connette un utente a un distributore
     * Verifica che il distributore sia attivo e non occupato da un altro utente
     *
     * @param userId L'ID dell'utente
     * @param machineInput L'ID o il nome del distributore
     * @throws MachineNotFoundException Se il distributore non esiste
     * @throws MachineUnavailableException Se il distributore è guasto, in manutenzione, disattivo o occupato
     */
    @Transactional
    public void connectUserToMachine(Long userId, String machineInput) {
        VendingMachine machine = findMachineByInput(machineInput);

        if (machine == null) throw new MachineNotFoundException("Distributore non trovato");

        if ("MANUTENZIONE".equals(machine.getStatus()) ||
                "GUASTA".equals(machine.getStatus()) ||
                "DISATTIVA".equals(machine.getStatus())) {
            throw new MachineUnavailableException("Distributore fuori servizio (" + machine.getStatus() + ")");
        }

        if (machine.getConnectedUserId() != null) {
            if (machine.getConnectedUserId().equals(userId)) return;
            throw new MachineUnavailableException("Distributore occupato da un altro utente");
        }

        machine.setConnectedUserId(userId);
        vendingMachineDao.save(machine);
    }

    /**
     * Disconnette un utente da un distributore
     *
     * @param userId L'ID dell'utente
     * @param machineId L'ID del distributore
     */
    @Transactional
    public void disconnectUserFromMachine(Long userId, Long machineId) {
        VendingMachine machine = vendingMachineDao.findById(machineId).orElse(null);
        if (machine != null && userId.equals(machine.getConnectedUserId())) {
            machine.setConnectedUserId(null);
            vendingMachineDao.save(machine);
        }
    }


    /**
     * Gestisce l'acquisto di un prodotto dal distributore fisico
     * Esegue i seguenti passaggi:
     * 1. Verifica la connessione dell'utente
     * 2. Recupera la ricetta del prodotto
     * 3. Verifica la disponibilità degli ingredienti (acqua, caffè, latte, zucchero)
     * 4. Verifica il credito dell'utente
     * 5. Aggiorna le scorte, il credito utente e l'incasso della macchina
     *
     * @param machineId L'ID del distributore
     * @param productName Il nome del prodotto selezionato
     * @param sugarLevel La quantità di zucchero aggiuntiva scelta dall'utente
     * @throws MachineNotFoundException Se la macchina non esiste
     * @throws UserNotConnectedException Se nessun utente è connesso
     * @throws IllegalArgumentException Se il prodotto non esiste
     * @throws MachineUnavailableException Se un ingrediente è esaurito
     * @throws InsufficientCreditException Se il credito è insufficiente
     */
    @Transactional
    public void buyFromMachine(Long machineId, String productName, int sugarLevel) {
        VendingMachine machine = vendingMachineDao.findById(machineId).orElseThrow(() -> new MachineNotFoundException("Macchina non trovata"));
        
        if (machine.getConnectedUserId() == null) {
            throw new UserNotConnectedException("Nessun utente connesso");
        }

        User user = userDao.findById(machine.getConnectedUserId()).orElseThrow(() -> new UserNotConnectedException("Utente connesso non valido"));

        Product p = productDao.findByName(productName)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non valido: " + productName));

        double price = p.getPrice();
        int waterCost = p.getWaterReq();
        int coffeeCost = p.getCoffeeReq();
        int milkCost = p.getMilkReq();
        int sugarCost = p.getSugarReq() + sugarLevel;

        //Disponibilità ingredienti
        if (machine.getWaterLevel() < waterCost) throw new MachineUnavailableException("Acqua esaurita");
        if (machine.getCoffeeLevel() < coffeeCost) throw new MachineUnavailableException("Caffè esaurito");
        if (machine.getMilkLevel() < milkCost) throw new MachineUnavailableException("Latte esaurito");
        if (machine.getSugarLevel() < sugarCost) throw new MachineUnavailableException("Zucchero esaurito");

        //Verifica credito
        if (user.getCredit() < price) {
            throw new InsufficientCreditException("Credito insufficiente");
        }

        //Esegui transazione con arrotondamento
        double newCredit = Math.round((user.getCredit() - price) * 100.0) / 100.0;
        user.setCredit(newCredit);

        double newCash = Math.round((machine.getCash() + price) * 100.0) / 100.0;
        machine.setCash(newCash);
        
        machine.setWaterLevel(machine.getWaterLevel() - waterCost);
        machine.setCoffeeLevel(machine.getCoffeeLevel() - coffeeCost);
        machine.setMilkLevel(machine.getMilkLevel() - milkCost);
        machine.setSugarLevel(machine.getSugarLevel() - sugarCost);

        userDao.save(user);
        vendingMachineDao.save(machine);
    }

    /**
     * Ripristina le scorte di un distributore al 100%
     *
     * @param machineId L'ID del distributore
     */
    @Transactional
    public void restockMachine(Long machineId) {
        VendingMachine machine = vendingMachineDao.findById(machineId).orElse(null);
        if (machine != null) {
            machine.setWaterLevel(100);
            machine.setCoffeeLevel(100);
            machine.setMilkLevel(100);
            machine.setSugarLevel(100);
            vendingMachineDao.save(machine);
        }
    }

    /**
     * Aggiorna lo stato di un distributore e notifica il sistema di monitoring esterno
     *
     * @param machineId L'ID del distributore
     * @param newStatus Il nuovo stato da impostare (es. ATTIVA, GUASTA)
     */
    @Transactional
    public void updateMachineStatus(Long machineId, String newStatus) {
        VendingMachine machine = vendingMachineDao.findById(machineId).orElse(null);
        if (machine != null) {
            machine.setStatus(newStatus);
            vendingMachineDao.save(machine);
            monitoringService.notifyStatusChange(machine.getLocation(), newStatus);
        }
    }

    /**
     * Aggiorna lo stato di un distributore basandosi sulla sua posizione
     * Utilizzato per la sincronizzazione dal sistema di monitoring esterno
     * Non invia notifiche al monitoring per evitare loop infiniti
     *
     * @param location La posizione del distributore
     * @param newStatus Il nuovo stato
     */
    @Transactional
    public void updateMachineStatusByLocation(String location, String newStatus) {
        VendingMachine machine = vendingMachineDao.findByLocation(location).orElse(null);
        if (machine != null) {
            machine.setStatus(newStatus);
            vendingMachineDao.save(machine);
        }
    }
}