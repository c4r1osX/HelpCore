package com.helpcore.notification_service.controlador;

import com.helpcore.notification_service.dto.TicketCreatedDto;
import com.helpcore.notification_service.servicios.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final EmailService emailService;

    @PostMapping("/ticket-creado")
    public ResponseEntity<String> handleTicketCreated(@RequestBody TicketCreatedDto ticket) {
        String subject = "Nuevo ticket creado: " + ticket.getTitulo();
        String body = "Se ha creado un nuevo ticket.\n\n" +
                "ID: " + ticket.getTicketId() + "\n" +
                "Título: " + ticket.getTitulo() + "\n" +
                "Descripción: " + ticket.getDescripcion() + "\n" +
                "Creado por: " + ticket.getCreadoPor();

        emailService.sendTicketCreatedEmail("joaquin.asr.16@gmail.com", subject, body);

        return ResponseEntity.ok("Notificación enviada al equipo de soporte.");
    }
}
