package com.helpcore.notification_service.dto;

import lombok.Data;

@Data
public class TicketCreatedDto {
    private Long ticketId;
    private String titulo;
    private String descripcion;
    private String creadoPor;
}
