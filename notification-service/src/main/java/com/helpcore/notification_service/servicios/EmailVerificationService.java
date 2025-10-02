package com.helpcore.notification_service.servicios;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ConcurrentHashMap<String, String> codes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public void sendVerificationCode(String email) throws Exception {
        String codigo = String.valueOf(100000 + random.nextInt(900000));
        codes.put(email, codigo);

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("codigo", codigo);

        String body = templateEngine.process("codigo-verificated", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("Código de verificación");
        helper.setText(body, true);

        mailSender.send(message);
    }

    public boolean validateCode(String email, String codigo) {
        String stored = codes.get(email);
        return stored != null && stored.equals(codigo);
    }
}
