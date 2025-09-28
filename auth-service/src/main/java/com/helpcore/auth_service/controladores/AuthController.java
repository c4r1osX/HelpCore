package com.helpcore.auth_service.controladores;

import com.helpcore.auth_service.servicios.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieService cookieService;

   @PostMapping("/register")
   public ResponseEntity<TokenResponseDTO> register(@RequestBody final UsuarioRegisterDTO request, HttpServletResponse response) {
       final TokenResponseDTO token = authService.registrar(request);
       cookieService.setAuthCookies(response, token.accessToken(), token.refreshToken());
       return ResponseEntity.ok(token);
   }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody final UsuarioLoginDTO request, HttpServletResponse response) {
        final TokenResponseDTO token = authService.login(request);
        cookieService.setAuthCookies(response, token.accessToken(), token.refreshToken());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,
                                         HttpServletResponse response) {
        final String accessToken = cookieService.getAccessToken(request);
        authService.logout(accessToken);
        cookieService.clearAuthCookies(response);
        return ResponseEntity.ok("Logout exitoso");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(HttpServletRequest request, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshToken(request);
        final TokenResponseDTO token = authService.refreshToken(refreshToken);
        cookieService.setAuthCookies(response, token.accessToken(), token.refreshToken());
        return ResponseEntity.ok(token);
    }

}
