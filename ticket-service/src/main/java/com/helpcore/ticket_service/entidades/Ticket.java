package com.helpcore.ticket_service.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Integer id;

    @Column(length = 200, nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.NUEVO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(name = "codigo_alumno", length = 15, nullable = false)
    private String codigoAlumno;

    @Column(length = 50, nullable = false)
    private String sede;

    // Relación con invitado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_invitado")
    private Invitado invitado;

    @Column(name = "id_usuario_cliente")
    private Integer idUsuarioCliente;

    @Column(name = "id_usuario_agente")
    private Integer idUsuarioAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria",referencedColumnName = "id_categoria")
    private CategoriaTicket categoria;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "es_activo", nullable = false)
    private boolean activo;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        activo = true;
        if (estado == null) estado = Estado.NUEVO;
        if (prioridad == null) prioridad = Prioridad.MEDIA;
    }

    public enum Estado {
        NUEVO, EN_ATENCION, RESUELTO, CERRADO
    }

    public enum Prioridad {
        BAJA, MEDIA, ALTA, URGENTE
    }
}