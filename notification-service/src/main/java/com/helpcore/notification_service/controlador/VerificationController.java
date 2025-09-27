package com.helpcore.notification_service.controlador;

import com.helpcore.notification_service.dto.EmailVerificationDto;
import com.helpcore.notification_service.servicios.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/enviar-correo")
    public ResponseEntity<String> sendCode(@RequestBody EmailVerificationDto request) {
        try {
            emailVerificationService.sendVerificationCode(request.getEmail(), request.getEmail());
            return ResponseEntity.ok("Código de verificación enviado a " + request.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar el correo: " + e.getMessage());
        }
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<String> validateCode(@RequestParam String email, @RequestParam String codigo) {
        boolean valido = emailVerificationService.validateCode(email, codigo);

        if (valido) {
            return ResponseEntity.ok("Código válido, correo verificado con éxito.");
        } else {
            return ResponseEntity.badRequest().body("Código incorrecto o expirado.");
        }
    }
}
