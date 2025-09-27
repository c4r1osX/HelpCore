package com.helpcore.auth_service.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpcore.auth_service.entidades.dto.login.TokenResponseDTO;
import com.helpcore.auth_service.entidades.dto.login.UsuarioLoginDTO;
import com.helpcore.auth_service.entidades.dto.login.UsuarioRegisterDTO;
import com.helpcore.auth_service.servicios.AuthService;

import org.springframework.http.HttpHeaders;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

   @PostMapping("/register")
   public ResponseEntity<TokenResponseDTO> register(@RequestBody final UsuarioRegisterDTO request) {
       final TokenResponseDTO token = authService.registrar(request);
       return ResponseEntity.ok(token);
   }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody final UsuarioLoginDTO request) {
       final TokenResponseDTO token = authService.login(request);
       return ResponseEntity.ok(token);
   }

   @PostMapping("/refresh")
    public TokenResponseDTO refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader) {
       return authService.refreshToken(authHeader);
   } 
}
