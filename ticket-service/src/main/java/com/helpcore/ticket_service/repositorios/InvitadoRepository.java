package com.helpcore.ticket_service.repositorios;

import com.helpcore.ticket_service.entidades.Invitado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitadoRepository extends JpaRepository<Invitado, Integer> {
}
