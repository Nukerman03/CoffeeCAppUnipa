package com.scaglione.coffeecappunipa.controller;

import com.scaglione.coffeecappunipa.entity.Product;
import com.scaglione.coffeecappunipa.entity.VendingMachine;
import com.scaglione.coffeecappunipa.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller MVC che gestisce la visualizzazione della pagina del distributore
 * Serve la pagina HTML che simula il display fisico della macchinetta
 */
@Controller
public class VendingMachineViewController {

    @Autowired
    private VendingMachineService vendingMachineService;

    /**
     * Mostra la pagina di interfaccia del distributore
     * Verifica che la macchina esista e sia attiva prima di mostrare la pagina
     * Carica i dati della macchina e il listino prodotti dal database per popolare la vista
     *
     * @param machineId L'ID del distributore da visualizzare
     * @param model Il modello per passare dati alla vista (ID, nome macchina, lista prodotti)
     * @return Il nome della vista "index_distributore"
     * @throws ResponseStatusException Se il distributore non esiste (404)
     */
    @GetMapping("/distributore/{machineId}")
    public String showDistributorPage(@PathVariable Long machineId, Model model) {
        VendingMachine machine = vendingMachineService.findMachineById(machineId);

        if (machine == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributore non trovato");
        }
        
        if ("DISATTIVA".equals(machine.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributore non attivo");
        }

        List<Product> products = vendingMachineService.getAllProducts();

        model.addAttribute("machineId", machineId);
        model.addAttribute("machineName", machine.getName());
        model.addAttribute("products", products);

        return "index_distributore";
    }
}