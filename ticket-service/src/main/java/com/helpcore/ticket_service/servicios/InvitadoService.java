package com.helpcore.ticket_service.servicios;

import com.helpcore.ticket_service.entidades.Invitado;
import com.helpcore.ticket_service.entidades.Ticket;
import com.helpcore.ticket_service.repositorios.InvitadoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvitadoService {

    @Autowired
    private InvitadoRepository invitadoRepository;

    public Invitado buscar(Integer id) {
        return invitadoRepository.findById(id).orElse(null);
    }

    public List<Invitado> listar() {
        return invitadoRepository.findAll();
    }

    public Invitado crear(Invitado invitado) {
        invitado.setActivo(true);
        return invitadoRepository.save(invitado);
    }

    public Invitado actualizar(Invitado invitado) {
        Invitado invitadoActual = buscar(invitado.getId());

        if (invitadoActual != null && invitadoActual.isActivo()) {
            invitadoActual.setNombre(invitado.getNombre());
            invitadoActual.setApellido(invitado.getApellido());
            invitadoActual.setDni(invitado.getDni());
            invitadoActual.setEmail(invitado.getEmail());
            invitadoActual.setTelefono(invitado.getTelefono());
            return invitadoRepository.save(invitadoActual);
        }

        return null;
    }

    public boolean eliminar(Integer id) {
        Invitado invitadoActual = buscar(id);
        if (invitadoActual != null && invitadoActual.isActivo()) {
            invitadoActual.setActivo(false);
            invitadoRepository.save(invitadoActual);
            return true;
        }
        return false;
    }
}