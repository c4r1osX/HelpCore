package com.helpcore.notification_service.controlador;

import com.helpcore.notification_service.dto.TicketCreatedDto;
import com.helpcore.notification_service.servicios.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final EmailService emailService;

    @PostMapping("/ticket-creado")
    public ResponseEntity<String> handleTicketCreated(@RequestBody TicketCreatedDto ticket) {
        try{
            emailService.sendTicketCreatedEmails(ticket, "joaquin.asr.16@gmail.com");
            return ResponseEntity.ok("Notificación enviada al equipo de soporte.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar el correo: " + e.getMessage());
        }
    }
}
