package com.helpcore.ticket_service.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_categoria_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;
}