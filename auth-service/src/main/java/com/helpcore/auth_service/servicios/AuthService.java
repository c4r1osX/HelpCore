package com.helpcore.auth_service.servicios;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;

import com.helpcore.auth_service.entidades.Token;
import com.helpcore.auth_service.entidades.Usuario;
import com.helpcore.auth_service.entidades.dto.login.TokenResponseDTO;
import com.helpcore.auth_service.entidades.dto.login.UsuarioLoginDTO;
import com.helpcore.auth_service.entidades.dto.login.UsuarioRegisterDTO;
import com.helpcore.auth_service.repositorios.TokenRepository;
import com.helpcore.auth_service.repositorios.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CookieService cookieService;

    public TokenResponseDTO registrar(UsuarioRegisterDTO dto) {
        Usuario usuario = Usuario.builder()
                .nombreUsuario(dto.getNombreUsuario())
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .activo(true)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        var jwtToken = jwtService.generarToken(usuario);
        var refreshToken = jwtService.generarRefreshToken(usuario);

        guardarTokenUsuario(usuarioGuardado, jwtToken);
        return new TokenResponseDTO(jwtToken, refreshToken);
    }

    public TokenResponseDTO login(UsuarioLoginDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNombreUsuario(),
                            request.getContrasena()));
        } catch (Exception ex) {
            throw new UsernameNotFoundException("Usuario o contraseña inválidos");
        }
        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        var jwtToken = jwtService.generarToken(usuario);
        var refreshToken = jwtService.generarRefreshToken(usuario);

        removerTokensUsuario(usuario);
        guardarTokenUsuario(usuario, jwtToken);

        return new TokenResponseDTO(jwtToken, refreshToken);
    }

    public TokenResponseDTO refreshToken(final String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Token inválido");
        }

        final String nombreUsuario = jwtService.extraerUsuario(refreshToken);

        if (nombreUsuario == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        final Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException(nombreUsuario));

        if (!jwtService.validarToken(refreshToken, usuario)) {
            throw new IllegalArgumentException("Token inválido");
        }

        final String nuevoToken = jwtService.generarToken(usuario);
        final String nuevoRefreshToken = jwtService.generarRefreshToken(usuario);

        removerTokensUsuario(usuario);
        guardarTokenUsuario(usuario, nuevoToken);

        return new TokenResponseDTO(nuevoToken, nuevoRefreshToken);
    }


    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        final Token tokenEncontrado = tokenRepository.findByToken(token).orElse(null);
        if (tokenEncontrado != null) {
            tokenEncontrado.setExpirado(true);
            tokenEncontrado.setRemovido(true);
            tokenRepository.save(tokenEncontrado);
        }
    }

    private void removerTokensUsuario(final Usuario usuario) {
        final List<Token> tokensUsuarioValido = tokenRepository.findAllValidTokensByUserId(usuario.getId());

        if (!tokensUsuarioValido.isEmpty()) {
            for (final Token token : tokensUsuarioValido) {
                token.setExpirado(true);
                token.setRemovido(true);
            }
            tokenRepository.saveAll(tokensUsuarioValido);
        }
    }

    private void guardarTokenUsuario(Usuario user, String jwtToken) {
        var token = Token.builder()
                .usuario(user)
                .token(jwtToken)
                .tipoToken(Token.TipoToken.BEARER)
                .expirado(false)
                .removido(false)
                .build();

        tokenRepository.save(token);
    }
}