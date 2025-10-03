package com.helpcore.ticket_service.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_invitado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitado")
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 100, nullable = false)
    private String apellido;

    @Column(length = 8, nullable = false, unique = true)
    private String dni;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "es_activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
    }
}
