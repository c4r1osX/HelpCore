package com.helpcore.ticket_service.repositorios;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaTicketRepository extends JpaRepository<RespuestaTicketRepository, Integer> {
}