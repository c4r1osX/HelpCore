package com.helpcore.auth_service.entidades.dto.login;

import lombok.Data;

@Data
public class UsuarioLoginDTO {
    private String nombreUsuario;
    private String contrasena;
}
