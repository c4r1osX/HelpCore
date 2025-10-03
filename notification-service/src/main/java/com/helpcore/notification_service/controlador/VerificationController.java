package com.helpcore.notification_service.controlador;

import com.helpcore.notification_service.dto.EmailVerificationDto;
import com.helpcore.notification_service.servicios.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/enviar-correo")
    public ResponseEntity<String> sendCode(@RequestBody EmailVerificationDto request) {
        try {
            emailVerificationService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok("Código de verificación enviado a " + request.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar el correo: " + e.getMessage());
        }
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<Map<String, Object>> validateCode(@RequestParam String email, @RequestParam String codigo) {
        boolean valido = emailVerificationService.validateCode(email, codigo);

        Map<String, Object> response = new HashMap<>();
        response.put("success", valido);
        response.put("message", valido ? "Código válido, correo verificado con éxito."
                : "Código incorrecto o expirado.");

        return valido ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}
