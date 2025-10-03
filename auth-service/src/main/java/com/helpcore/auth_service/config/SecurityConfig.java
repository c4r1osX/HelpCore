package com.helpcore.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.helpcore.auth_service.entidades.Token;
import com.helpcore.auth_service.repositorios.TokenRepository;
import com.helpcore.auth_service.repositorios.UsuarioRepository;
import com.helpcore.auth_service.seguridad.JwtAuthFilter;
import com.helpcore.auth_service.servicios.CookieService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Lazy
    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final CookieService cookieService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Usuario o contraseña inválidos\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            final String token = cookieService.getAccessToken(request);
                            expireToken(token);
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Limpia contexto y cookies al hacer logout
                            org.springframework.security.core.context.SecurityContextHolder.clearContext();
                            cookieService.clearAuthCookies(response);
                        })
                );

        return http.build();
    }

    private void expireToken(final String token) {
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}