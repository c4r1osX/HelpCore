package com.helpcore.notification_service.servicios;

import com.helpcore.notification_service.dto.TicketCreatedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendTicketCreatedEmails(TicketCreatedDto ticket, String soporte) {
        String subject = "Nuevo ticket creado: " + ticket.getTitulo();

        sendEmailWithTemplate(
                ticket.getCorreoCreador(),
                subject,
                ticket,
                "✅ Por favor, atento a su ticket, se le dará respuesta pronto."
        );

        sendEmailWithTemplate(
                soporte,
                subject,
                ticket,
                "✅ Por favor, revise el ticket lo antes posible"
        );
    }

    private void sendEmailWithTemplate(String creador, String subject, TicketCreatedDto ticket, String mensajePersonalizado) {
        Context context = new Context();
        context.setVariable("ticketId", ticket.getTicketId());
        context.setVariable("titulo", ticket.getTitulo());
        context.setVariable("descripcion", ticket.getDescripcion());
        context.setVariable("correoCreador", ticket.getCorreoCreador());
        context.setVariable("soporte", mensajePersonalizado);

        String body = templateEngine.process("ticket-created", context);

        MimeMessagePreparator message = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(creador);
            helper.setSubject(subject);
            helper.setText(body, true);
        };

        mailSender.send(message);
    }
}
