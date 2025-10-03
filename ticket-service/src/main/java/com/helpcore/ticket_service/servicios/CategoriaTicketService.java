package com.helpcore.ticket_service.servicios;

import com.helpcore.ticket_service.entidades.CategoriaTicket;
import com.helpcore.ticket_service.repositorios.CategoriaTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaTicketService {
    @Autowired
    CategoriaTicketRepository categoriaTicketRepository;

    public CategoriaTicket buscar(Integer id){
        return categoriaTicketRepository.findById(id).orElse(null);
    }

    public List<CategoriaTicket> listar(){
        return categoriaTicketRepository.findAll();
    }
}
