package com.scaglione.coffeecappunipa.controller;

import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

/**
 * Controller per la gestione delle operazioni di manutenzione
 * Accessibile solo agli utenti con ruolo MAINTAINER
 * Permette di consultare lo stato dei distributori, ripristinare le scorte e aggiornare lo stato
 */
@Controller
public class MaintenanceController {

    @Autowired
    private VendingMachineService vendingMachineService;
    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Mostra la dashboard principale del manutentore
     * Accessibile solo agli utenti con ruolo MAINTAINER
     *
     * @return Il nome della vista "index_manutenzione"
     */
    @GetMapping("/maintenance")
    public String showMaintenancePage() {
        return "index_manutenzione";
    }

    /**
     * Cerca un distributore specifico per visualizzarne lo stato tecnico
     * Accetta sia richieste GET (dai redirect) che POST (dal form)
     * Accessibile solo agli utenti con ruolo MAINTAINER
     *
     * @param machineId L'ID o il nome del distributore da cercare
     * @param model Il modello per passare dati alla vista
     * @return Il nome della vista "index_manutenzione"
     */
    @RequestMapping(value = "/maintenance/search", method = {RequestMethod.GET, RequestMethod.POST})
    public String searchMachine(@RequestParam String machineId, Model model) {
        try {
            VendingMachine machine = vendingMachineService.findMachineByInput(machineId);
            
            if (machine != null) {
                model.addAttribute("machine", machine);
            } else {
                model.addAttribute("error", "Distributore non trovato");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Errore nella ricerca");
        }
        return "index_manutenzione";
    }

    /**
     * Genera ed esporta lo stato completo del parco macchine in formato XML
     * Utilizza Thymeleaf per processare il template XML dinamicamente
     * Accessibile solo agli utenti con ruolo MAINTAINER
     *
     * @return ResponseEntity contenente il file XML scaricabile
     */
    @GetMapping(value = "/maintenance/export/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> exportXml() {
        List<VendingMachine> machines = vendingMachineService.getAllMachines();
        Context context = new Context();
        context.setVariable("machines", machines);

        String xmlContent = templateEngine.process("xml/macchine", context);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=macchine.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(xmlContent);
    }

    /**
     * Ripristina le scorte di un distributore (acqua, caffè, ecc.) al 100%
     * Accessibile solo agli utenti con ruolo MAINTAINER
     *
     * @param id L'ID del distributore
     * @param machineName Il nome del distributore (usato per il redirect)
     * @return Redirect alla pagina di ricerca per mostrare i dati aggiornati
     */
    @PostMapping("/maintenance/restock")
    public String restockMachine(@RequestParam Long id, @RequestParam String machineName) {
        vendingMachineService.restockMachine(id);
        return "redirect:/maintenance/search?machineId=" + machineName;
    }

    /**
     * Aggiorna lo stato operativo di un distributore
     * Accessibile solo agli utenti con ruolo MAINTAINER
     *
     * @param id L'ID del distributore
     * @param status Il nuovo stato da impostare
     * @param machineName Il nome del distributore (usato per il redirect)
     * @return Redirect alla pagina di ricerca per mostrare i dati aggiornati
     */
    @PostMapping("/maintenance/status")
    public String updateStatus(@RequestParam Long id, @RequestParam String status, @RequestParam String machineName) {
        vendingMachineService.updateMachineStatus(id, status);
        return "redirect:/maintenance/search?machineId=" + machineName;
    }
}