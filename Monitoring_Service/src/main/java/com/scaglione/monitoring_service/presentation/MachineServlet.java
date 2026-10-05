package com.scaglione.monitoring_service.presentation;

import com.scaglione.monitoring_service.business.MachineService;
import com.scaglione.monitoring_service.data.MachineStatus;
import com.scaglione.monitoring_service.dto.MachineDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet che fornisce endpoint per recuperare, aggiungere, aggiornare e rimuovere macchine.
 */
@WebServlet(name = "MachineServlet", value = "/machines")
public class MachineServlet extends HttpServlet {

    @Inject
    private MachineService machineService;

    private final ObjectMapper mapper;

    public MachineServlet() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    /**
     * Gestisce le richieste GET per recuperare la lista di tutte le macchine.
     * Restituisce un array JSON contenente i dettagli di tutte le macchine registrate.
     *
     * @param req  La richiesta HTTP
     * @param resp La risposta HTTP
     * @throws ServletException Se si verifica un errore specifico della servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<MachineDTO> machines = machineService.getAllMachines();
            sendJson(resp, HttpServletResponse.SC_OK, mapper.writeValueAsString(machines));
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Gestisce le richieste POST per registrare una nuova macchina.
     * Riceve un oggetto JSON con i dati della macchina e la aggiunge al sistema.
     *
     * @param req  La richiesta HTTP contenente il JSON della nuova macchina
     * @param resp La risposta HTTP
     * @throws ServletException Se si verifica un errore specifico della servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            MachineDTO newMachineDto = mapper.readValue(req.getReader(), MachineDTO.class);
            machineService.addMachine(newMachineDto);
            sendJson(resp, HttpServletResponse.SC_CREATED, "{\"message\": \"Distributore aggiunto con successo\"}");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Gestisce le richieste DELETE per rimuovere una macchina.
     * Identifica la macchina da rimuovere tramite il parametro 'locationLabel'.
     *
     * @param req  La richiesta HTTP contenente il parametro 'locationLabel'
     * @param resp La risposta HTTP
     * @throws ServletException Se si verifica un errore specifico della servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String locationLabel = req.getParameter("locationLabel");
        if (locationLabel == null || locationLabel.isEmpty()) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Parametro 'locationLabel' mancante");
            return;
        }

        try {
            machineService.removeMachine(locationLabel);
            sendJson(resp, HttpServletResponse.SC_OK, "{\"message\": \"Operazione di rimozione completata\"}");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * Gestisce le richieste PUT per aggiornare lo stato di una macchina.
     * Riceve un JSON con 'locationLabel' e l'azione da eseguire (es. cambio stato).
     *
     * @param req  La richiesta HTTP contenente il JSON di aggiornamento
     * @param resp La risposta HTTP
     * @throws ServletException Se si verifica un errore specifico della servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            MachineDTO dto = mapper.readValue(req.getReader(), MachineDTO.class);

            String locationLabel = dto.getLocationLabel();
            String action = dto.getAction();

            if (locationLabel == null || action == null) throw new Exception("Dati mancanti (locationLabel o action)");

            MachineStatus status;
            if (action.equalsIgnoreCase("ATTIVA")) status = MachineStatus.ATTIVA;
            else if (action.equalsIgnoreCase("MANUTENZIONE")) status = MachineStatus.MANUTENZIONE;
            else throw new Exception("Azione non valida: usa ATTIVA o MANUTENZIONE");

            machineService.updateStatus(locationLabel, status);
            sendJson(resp, HttpServletResponse.SC_OK, "{\"message\": \"Stato aggiornato\"}");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void sendJson(HttpServletResponse resp, int code, String json) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        String cleanMessage = message != null ? message.replace("\"", "'") : "Errore sconosciuto";
        sendJson(resp, code, "{\"error\": \"" + cleanMessage + "\"}");
    }
}