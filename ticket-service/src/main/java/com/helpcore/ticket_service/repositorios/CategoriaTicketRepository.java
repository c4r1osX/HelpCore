package com.helpcore.ticket_service.repositorios;

import com.helpcore.ticket_service.entidades.CategoriaTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaTicketRepository extends JpaRepository<CategoriaTicket,Integer> {
}
