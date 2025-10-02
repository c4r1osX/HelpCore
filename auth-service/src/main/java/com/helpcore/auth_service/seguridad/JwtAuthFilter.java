    package com.helpcore.auth_service.seguridad;

    import java.io.IOException;
    import java.util.Optional;

    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import com.helpcore.auth_service.entidades.Token;
    import com.helpcore.auth_service.entidades.Usuario;
    import com.helpcore.auth_service.repositorios.TokenRepository;
    import com.helpcore.auth_service.repositorios.UsuarioRepository;
    import com.helpcore.auth_service.servicios.JwtService;
    import com.helpcore.auth_service.servicios.CookieService;

    import io.micrometer.common.lang.NonNull;
    import jakarta.servlet.FilterChain;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import lombok.RequiredArgsConstructor;

    @Component
    @RequiredArgsConstructor
    public class JwtAuthFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;
        private final TokenRepository tokenRepository;
        private final UsuarioRepository usuarioRepository;
        private final CookieService cookieService;

        @Override
        protected void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain) throws ServletException, IOException {

            if (request.getServletPath().contains("/auth")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwtToken = cookieService.getAccessToken(request);

            if (jwtToken == null || jwtToken.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            final String nombreUsuario = jwtService.extraerUsuario(jwtToken);

            if (nombreUsuario == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            final Token token = tokenRepository.findByToken(jwtToken).orElse(null);

            if (token == null || token.isExpirado() || token.isRemovido()) {
                filterChain.doFilter(request, response);
                return;
            }

            final Usuario loadUser = usuarioRepository.findByNombreUsuario(nombreUsuario)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(loadUser.getNombreUsuario())
                    .password(loadUser.getContrasena())
                    .build();

            final Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(userDetails.getUsername());

            if (usuario.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            final boolean isTokenValid = jwtService.validarToken(jwtToken, usuario.get());

            if (!isTokenValid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Token inválido o expirado\"}");
                return;
            }

            final var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        }
    }