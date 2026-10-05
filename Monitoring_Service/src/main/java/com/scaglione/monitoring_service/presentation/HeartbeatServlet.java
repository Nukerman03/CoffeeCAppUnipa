package com.scaglione.monitoring_service.presentation;

import com.scaglione.monitoring_service.business.MachineService;
import com.scaglione.monitoring_service.dto.MachineDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Endpoint dedicato alla ricezione dei segnali di vita (heartbeat) dalle macchine.
 * Accetta richieste POST contenenti l'identificativo della macchina (locationLabel)
 * e aggiorna il timestamp dell'ultimo contatto nel sistema.
 */
@WebServlet(name = "HeartbeatServlet", value = "/heartbeat")
public class HeartbeatServlet extends HttpServlet {

    @Inject
    private MachineService machineService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger LOGGER = Logger.getLogger(HeartbeatServlet.class.getName());

    /**
     * Gestisce la ricezione di un heartbeat.
     * Legge il JSON dalla richiesta, estrae la 'locationLabel' e invoca il servizio
     * per registrare l'evento.
     *
     * @param req  La richiesta HTTP contenente il payload JSON
     * @param resp La risposta HTTP
     * @throws ServletException Se si verifica un errore specifico della servlet
     * @throws IOException      Se si verifica un errore di I/O
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            MachineDTO dto = mapper.readValue(req.getReader(), MachineDTO.class);

            if (dto.getLocationLabel() != null && !dto.getLocationLabel().isEmpty()) {
                machineService.recordHeartbeatByLocation(dto.getLocationLabel());
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"LocationLabel mancante nel JSON\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\": \"received\"}");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'elaborazione dell'heartbeat", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}