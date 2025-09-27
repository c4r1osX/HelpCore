package com.helpcore.ticket_service.controladores;

import com.helpcore.ticket_service.entidades.CategoriaTicket;
import com.helpcore.ticket_service.servicios.CategoriaTicketService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categoria-ticket")
public class CategoriaTicketController {
    @Autowired
    CategoriaTicketService categoriaTicketService;

    @GetMapping("/listar")
    public ResponseEntity<List<CategoriaTicket>> listarCategoriaTicket(){

        List<CategoriaTicket> listaTickets = categoriaTicketService.listar();

        if(listaTickets == null){
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(categoriaTicketService.listar());
    }
}
