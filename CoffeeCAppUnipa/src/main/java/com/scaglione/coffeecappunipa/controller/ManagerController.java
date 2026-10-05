package com.scaglione.coffeecappunipa.controller;

import com.scaglione.coffeecappunipa.dto.UserDto;
import com.scaglione.coffeecappunipa.entity.User;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.service.UserService;
import com.scaglione.coffeecappunipa.service.VendingMachineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

/**
 * Controller per la gestione amministrativa del sistema
 * Accessibile solo agli utenti con ruolo MANAGER
 * Gestisce il parco macchine, i manutentori e l'export dei dati
 */
@Controller
public class ManagerController {

    @Autowired
    private UserService userService;
    @Autowired
    private VendingMachineService vendingMachineService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Mostra la dashboard principale del gestore
     * Carica le liste di manutentori e distributori per la visualizzazione
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @param model Il modello per passare dati alla vista
     * @return Il nome della vista "dashboard"
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("maintainers", userService.getMaintainers());
        model.addAttribute("vendingMachines", vendingMachineService.getAllMachines());
        return "dashboard";
    }

    /**
     * Aggiunge un nuovo manutentore al sistema
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @param userDto DTO contenente i dati del nuovo manutentore
     * @return Redirect alla dashboard con messaggio di successo o errore
     */
    @PostMapping("/dashboard/add-maintainer")
    public String addMaintainer(@ModelAttribute UserDto userDto) {
        boolean success = userService.registerMaintainer(userDto);
        if (success) {
            return "redirect:/dashboard?success=Manutentore creato";
        } else {
            return "redirect:/dashboard?error=Username esistente";
        }
    }

    /**
     * Aggiunge un nuovo distributore al sistema
     * Notifica il servizio di Monitoring esterno dell'avvenuta creazione
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @param machine L'entità VendingMachine da salvare
     * @return Redirect alla dashboard con messaggio di successo o errore
     */
    @PostMapping("/dashboard/add-machine")
    public String addMachine(@ModelAttribute VendingMachine machine) {
        try {
            vendingMachineService.createMachine(machine);
            return "redirect:/dashboard?success=Distributore installato";
        } catch (RuntimeException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }
    
    /**
     * Elimina un manutentore dal sistema
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @param id L'ID del manutentore da eliminare
     * @return Redirect alla dashboard
     */
    @PostMapping("/dashboard/delete-maintainer")
    public String deleteMaintainer(@RequestParam Long id) {
        try {
            userService.deleteUser(id);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    /**
     * Elimina un distributore dal sistema
     * Notifica il servizio di Monitoring esterno dell'avvenuta rimozione
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @param id L'ID del distributore da eliminare
     * @return Redirect alla dashboard
     */
    @PostMapping("/dashboard/delete-machine")
    public String deleteMachine(@RequestParam Long id) {
        try {
            vendingMachineService.deleteMachine(id);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            return "redirect:/dashboard?error=" + e.getMessage();
        }
    }

    /**
     * Genera ed esporta l'elenco dei manutentori in formato XML
     * Utilizza Thymeleaf per processare il template XML dinamicamente
     * Accessibile solo agli utenti con ruolo MANAGER
     *
     * @return ResponseEntity contenente il file XML scaricabile
     */
    @GetMapping(value = "/dashboard/export/addetti", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportAddettiXml() {
        List<User> maintainers = userService.getMaintainers();
        Context context = new Context();
        context.setVariable("maintainers", maintainers);

        String xmlContent = templateEngine.process("xml/addetti", context);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=addetti.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(xmlContent);
    }
}