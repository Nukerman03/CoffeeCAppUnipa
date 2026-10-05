package com.scaglione.coffeecappunipa.service;

import com.scaglione.coffeecappunipa.dao.RoleDao;
import com.scaglione.coffeecappunipa.dao.UserDao;
import com.scaglione.coffeecappunipa.dto.UserDto;
import com.scaglione.coffeecappunipa.entity.Role;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.utils.RoleNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service per la gestione degli utenti e delle operazioni correlate
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private VendingMachineService vendingMachineService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Recupera un utente tramite username
     *
     * @param username L'username dell'utente
     * @return L'oggetto User se trovato
     * @throws UsernameNotFoundException se l'utente non viene trovato
     */
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + username));
    }

    /**
     * Recupera la lista di tutti i manutentori
     *
     * @return Lista di utenti con ruolo MAINTAINER
     */
    public List<User> getMaintainers() {
        return userDao.findByRoleName(RoleNames.MAINTAINER);
    }

    /**
     * Elimina un utente dal sistema
     *
     * @param id L'ID dell'utente da eliminare
     * @throws RuntimeException Se l'utente non può essere eliminato
     */
    @Transactional
    public void deleteUser(Long id) {
        try {
            userDao.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile eliminare: elemento in uso o inesistente");
        }
    }

    /**
     * Registra un nuovo utente standard nel sistema
     * Assegna automaticamente il ruolo ROLE_USER e cripta la password
     *
     * @param userDto DTO contenente i dati dell'utente
     * @return true se la registrazione ha successo, false se l'username esiste già
     */
    public boolean registerUser(UserDto userDto) {
        if (userDao.findByUsername(userDto.getUsername()).isPresent()) return false;

        User newUser = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()));
        Role userRole = roleDao.findByName(RoleNames.USER)
                .orElseThrow(() -> new RuntimeException("Errore critico: Ruolo " + RoleNames.USER + " non trovato!"));

        newUser.setRole(userRole);
        userDao.save(newUser);
        return true;
    }

    /**
     * Registra un nuovo manutentore nel sistema
     * Assegna automaticamente il ruolo ROLE_MAINTAINER e cripta la password
     *
     * @param userDto DTO contenente i dati del manutentore
     * @return true se la registrazione ha successo, false se l'username esiste già
     */
    public boolean registerMaintainer(UserDto userDto) {
        if (userDao.findByUsername(userDto.getUsername()).isPresent()) return false;

        User newUser = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()));
        Role maintainerRole = roleDao.findByName(RoleNames.MAINTAINER)
                .orElseThrow(() -> new RuntimeException("Errore critico: Ruolo " + RoleNames.MAINTAINER + " non trovato!"));

        newUser.setRole(maintainerRole);
        userDao.save(newUser);
        return true;
    }

    /**
     * Recupera un utente tramite il suo ID
     *
     * @param id L'ID dell'utente
     * @return L'oggetto User se trovato, altrimenti null
     */
    public User getUserById(Long id) {
        return userDao.findById(id).orElse(null);
    }

    /**
     * Ricarica il credito di un utente
     * Operazione transazionale
     *
     * @param userId L'ID dell'utente
     * @param amount L'importo da aggiungere al credito attuale
     */
    public void rechargeCredit(Long userId, Double amount) {
        User user = userDao.findById(userId).orElseThrow();
        user.setCredit(user.getCredit() + amount);
        userDao.save(user);
    }

    /**
     * Verifica se l'utente ha il ruolo di manutentore
     *
     * @param user L'utente da verificare
     * @return true se è un manutentore, false altrimenti
     */
    public boolean isUserMaintainer(User user) {
        return (user.getRole() != null && RoleNames.MAINTAINER.equals(user.getRole().getName()));
    }

    /**
     * Verifica se l'utente ha il ruolo di gestore
     *
     * @param user L'utente da verificare
     * @return true se è un gestore, altrimenti false
     */
    public boolean isUserManager(User user) {
        return (user.getRole() != null && RoleNames.MANAGER.equals(user.getRole().getName()));
    }

    /**
     * Costruisce il DTO per la homepage dell'utente
     * Aggrega i dati dell'utente (credito, username) e le informazioni sulla macchinetta eventualmente connessa
     *
     * @param userId L'ID dell'utente
     * @param connectedMachineId L'ID della macchinetta connessa
     * @return Un oggetto UserDto popolato con i dati necessari per la vista
     */
    public UserDto getUserDtoForHome(Long userId, Long connectedMachineId) {
        User user = getUserById(userId);
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setCredit(user.getCredit());

        if (connectedMachineId != null) {
            VendingMachine machine = vendingMachineService.findMachineById(connectedMachineId);

            if (machine != null && userId.equals(machine.getConnectedUserId())) {
                dto.setConnectedMachineDisplay(machine.getName());
                dto.setConnectedMachineId(machine.getId());
            }
        }
        return dto;
    }
}