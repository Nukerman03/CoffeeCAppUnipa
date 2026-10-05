package com.scaglione.coffeecappunipa.controller;

import com.scaglione.coffeecappunipa.dto.UserDto;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.exception.MachineNotFoundException;
import com.scaglione.coffeecappunipa.exception.MachineUnavailableException;
import com.scaglione.coffeecappunipa.service.UserService;
import com.scaglione.coffeecappunipa.service.VendingMachineService;
import com.scaglione.coffeecappunipa.utils.RoleNames;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller principale per la gestione delle operazioni utente
 * Gestisce l'autenticazione, la navigazione tra le pagine e le interazioni di base
 * come la connessione ai distributori e la ricarica del credito
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private VendingMachineService vendingMachineService;

    /**
     * Mostra la pagina di login
     * Se l'utente è già autenticato, viene reindirizzato automaticamente alla dashboard
     * appropriata in base al suo ruolo (Manager, Manutentore o Utente standard)
     *
     * @param authentication L'oggetto Authentication di Spring Security
     * @return Il nome della vista "login" o un redirect
     */
    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            if (RoleNames.MANAGER.equals(role)) {
                return "redirect:/dashboard";
            } else if (RoleNames.MAINTAINER.equals(role)) {
                return "redirect:/maintenance";
            } else {
                return "redirect:/home";
            }
        }
        return "login";
    }

    /**
     * Mostra la pagina di registrazione
     * Come per il login, se l'utente è già autenticato viene reindirizzato alla sua dashboard
     *
     * @param authentication L'oggetto Authentication di Spring Security
     * @return Il nome della vista "register" o un redirect
     */
    @GetMapping("/register")
    public String showRegisterPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            if (RoleNames.MANAGER.equals(role)) {
                return "redirect:/dashboard";
            } else if (RoleNames.MAINTAINER.equals(role)) {
                return "redirect:/maintenance";
            } else {
                return "redirect:/home";
            }
        }
        return "register";
    }

    /**
     * Mostra la dashboard principale dell'utente (Home)
     * Carica i dati dell'utente e lo stato dell'eventuale connessione a un distributore
     *
     * @param session La sessione HTTP corrente
     * @param model Il modello per passare dati alla vista
     * @param principal L'utente autenticato
     * @return Il nome della vista "home"
     */
    @GetMapping("/home")
    public String home(HttpSession session, Model model, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Long userId = user.getId();

        Long connectedMachineId = (Long) session.getAttribute("connectedMachineId");
        UserDto userDto = userService.getUserDtoForHome(userId, connectedMachineId);

        model.addAttribute("username", userDto.getUsername());
        model.addAttribute("credit", userDto.getCredit());
        
        if (userDto.getConnectedMachineId() != null) {
            model.addAttribute("connectedMachineName", userDto.getConnectedMachineDisplay());
            model.addAttribute("machineId", userDto.getConnectedMachineId());
        } else {
            session.removeAttribute("connectedMachineId");
            model.addAttribute("connectedMachineName", null);
        }

        return "home";
    }

    /**
     * Gestisce la registrazione di un nuovo utente
     *
     * @param userDto DTO contenente i dati del nuovo utente
     * @param model Il modello per passare messaggi di errore
     * @return Redirect al login in caso di successo o ritorno alla vista register in caso di errore
     */
    @PostMapping("/register")
    public String register(@ModelAttribute UserDto userDto, Model model) {
        try {
            boolean success = userService.registerUser(userDto);
            if (success) {
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Username già in uso");
                return "register";
            }
        } catch (RuntimeException e) {
            model.addAttribute("error", "Errore sistema: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Connette l'utente a un distributore specifico
     *
     * @param machineId L'ID o il nome del distributore
     * @param session La sessione HTTP corrente
     * @param principal L'utente autenticato
     * @return Redirect alla home con messaggio di successo o errore
     */
    @PostMapping("/connect")
    public String connect(@RequestParam String machineId, HttpSession session, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Long userId = user.getId();

        try {
            vendingMachineService.connectUserToMachine(userId, machineId);

            VendingMachine machine = vendingMachineService.findMachineByInput(machineId);
            if (machine != null) {
                session.setAttribute("connectedMachineId", machine.getId());
            }

            return "redirect:/home?msg=Connessione riuscita!";
        } catch (MachineNotFoundException | MachineUnavailableException e) {
            return "redirect:/home?msg=Errore: " + e.getMessage();
        }
    }

    /**
     * Disconnette l'utente dal distributore attuale
     * Libera la macchina rendendola disponibile per altri utenti
     *
     * @param session La sessione HTTP corrente
     * @param principal L'utente autenticato
     * @return Redirect alla home con messaggio di conferma
     */
    @PostMapping("/disconnect")
    public String disconnect(HttpSession session, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Long userId = user.getId();
        Long machineId = (Long) session.getAttribute("connectedMachineId");

        if (machineId != null) {
            vendingMachineService.disconnectUserFromMachine(userId, machineId);
            session.removeAttribute("connectedMachineId");
        }
        return "redirect:/home?msg=Disconnessione effettuata";
    }

    /**
     * Ricarica il credito dell'utente
     *
     * @param amount L'importo da ricaricare
     * @param principal L'utente autenticato
     * @return Redirect alla home con messaggio di conferma
     */
    @PostMapping("/recharge")
    public String recharge(@RequestParam Double amount, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        userService.rechargeCredit(user.getId(), amount);
        return "redirect:/home?msg=Ricarica effettuata";
    }

    /**
     * Endpoint API per recuperare lo stato aggiornato dell'utente (credito)
     * Utilizzato dal polling JavaScript nella home page per aggiornare l'interfaccia in tempo reale
     *
     * @param principal L'utente autenticato
     * @return ResponseEntity contenente un JSON con username e credito aggiornato
     */
    @GetMapping("/api/user/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatus(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("credit", user.getCredit());
        response.put("username", user.getUsername());
        
        return ResponseEntity.ok(response);
    }
}