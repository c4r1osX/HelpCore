package com.helpcore.ticket_service.controladores;

import com.helpcore.ticket_service.entidades.CategoriaTicket;
import com.helpcore.ticket_service.entidades.Invitado;
import com.helpcore.ticket_service.entidades.Ticket;
import com.helpcore.ticket_service.repositorios.CategoriaTicketRepository;
import com.helpcore.ticket_service.servicios.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    TicketService ticketService;

    @Autowired
    CategoriaTicketRepository categoriaTicketRepository;

    @PostMapping("/crear-invitado")
    public ResponseEntity<Map<String, Object>> crearTicketDesdeFormulario(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Crear objeto Invitado con los datos del formulario
            Invitado invitado = new Invitado();
            invitado.setNombre((String) requestData.get("nombres"));
            invitado.setApellido((String) requestData.get("apellidos"));
            invitado.setDni((String) requestData.get("dni"));
            invitado.setEmail((String) requestData.get("email"));
            invitado.setTelefono((String) requestData.get("telefono"));

            // Crear objeto Ticket con los datos del formulario
            Ticket ticket = new Ticket();
            ticket.setTitulo((String) requestData.get("asunto"));
            ticket.setDescripcion((String) requestData.get("comentarios"));
            ticket.setCodigoAlumno((String) requestData.get("codigoAlumno"));
            ticket.setSede((String) requestData.get("sede"));

            Integer idCategoria = (Integer) requestData.get("categoria");


            // Validaciones básicas
            if (invitado.getNombre() == null || invitado.getNombre().trim().isEmpty()) {
                response.put("error", "Los nombres son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }

            if (invitado.getApellido() == null || invitado.getApellido().trim().isEmpty()) {
                response.put("error", "Los apellidos son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }

            if (invitado.getDni() == null || invitado.getDni().trim().isEmpty()) {
                response.put("error", "El DNI es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            if (invitado.getEmail() == null || invitado.getEmail().trim().isEmpty()) {
                response.put("error", "El email es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            if (ticket.getTitulo() == null || ticket.getTitulo().trim().isEmpty()) {
                response.put("error", "El asunto es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            if (ticket.getDescripcion() == null || ticket.getDescripcion().trim().isEmpty()) {
                response.put("error", "Los comentarios son obligatorios");
                return ResponseEntity.badRequest().body(response);
            }

            // Crear el ticket con invitado
            Ticket nuevoTicket = ticketService.crearTicketConInvitado(ticket, invitado, idCategoria);

            response.put("success", true);
            response.put("message", "Ticket creado exitosamente");
            response.put("ticketId", nuevoTicket.getId());
            response.put("ticket", nuevoTicket);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error al crear el ticket: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
