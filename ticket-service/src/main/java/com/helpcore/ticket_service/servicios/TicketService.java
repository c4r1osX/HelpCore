package com.helpcore.ticket_service.servicios;


import com.helpcore.ticket_service.entidades.CategoriaTicket;
import com.helpcore.ticket_service.entidades.Invitado;
import com.helpcore.ticket_service.entidades.Ticket;
import com.helpcore.ticket_service.repositorios.CategoriaTicketRepository;
import com.helpcore.ticket_service.repositorios.InvitadoRepository;
import com.helpcore.ticket_service.repositorios.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private InvitadoService invitadoService;

    @Autowired
    private CategoriaTicketRepository categoriaTicketRepository;

    public Ticket buscar(Integer id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public List<Ticket> listar() {
        return ticketRepository.findAll();
    }

    public Ticket crear(Ticket ticket) {
        ticket.setActivo(true);
        return ticketRepository.save(ticket);
    }

    public Ticket actualizar(Ticket ticket) {
        Ticket ticketActual = buscar(ticket.getId());

        if (ticketActual != null && ticketActual.isActivo()) {
            ticketActual.setTitulo(ticket.getTitulo());
            ticketActual.setDescripcion(ticket.getDescripcion());
            ticketActual.setEstado(ticket.getEstado());
            ticketActual.setPrioridad(ticket.getPrioridad());
            ticketActual.setCodigoAlumno(ticket.getCodigoAlumno());
            ticketActual.setSede(ticket.getSede());
            ticketActual.setIdUsuarioCliente(ticket.getIdUsuarioCliente());
            ticketActual.setIdUsuarioAgente(ticket.getIdUsuarioAgente());
            ticketActual.setCategoria(ticket.getCategoria());
            ticketActual.setInvitado(ticket.getInvitado());
            ticketActual.setFechaAsignacion(ticket.getFechaAsignacion());
            ticketActual.setFechaResolucion(ticket.getFechaResolucion());
            ticketActual.setFechaCierre(ticket.getFechaCierre());

            return ticketRepository.save(ticketActual);
        }
        return null;
    }

    public boolean eliminar(Integer id) {
        Ticket ticketActual = buscar(id);
        if (ticketActual != null && ticketActual.isActivo()) {
            ticketActual.setActivo(false);
            ticketRepository.save(ticketActual);
            return true;
        }
        return false;
    }

    /**
     * Método para crear ticket con invitado usando las entidades directamente
     * El objeto ticket debe venir con el invitado ya configurado
     */
    @Transactional
    public Ticket crearTicketConInvitado(Ticket ticket, Invitado invitado, Integer idCategoria) {
        try {
            Invitado invitadoGuardado = invitadoService.crear(invitado);

            ticket.setInvitado(invitadoGuardado);
            ticket.setIdUsuarioCliente(null);
            ticket.setEstado(Ticket.Estado.NUEVO);
            ticket.setPrioridad(Ticket.Prioridad.MEDIA);
            ticket.setFechaCreacion(LocalDateTime.now());

            CategoriaTicket categoria = categoriaTicketRepository.findById(idCategoria)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            ticket.setCategoria(categoria);

            return crear(ticket);

        } catch (Exception e) {
            throw new RuntimeException("Error al crear el ticket con invitado: " + e.getMessage(), e);
        }
    }
}