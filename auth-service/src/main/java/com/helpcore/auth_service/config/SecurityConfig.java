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


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Usuario o contraseña inválidos\"}");
            })
        )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> 
                    logout.logoutUrl("auth/logout")
                    .addLogoutHandler((request, response, authentication) -> {
                        final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                        logout(authHeader);
                    })
                    .logoutSuccessHandler((request, response, authentication) -> 
                            SecurityContextHolder.clearContext())
                    
            ); 

        return http.build();
    }

    private void logout(final String token) {
        if(token == null || !token.startsWith("Bearer ")){
            throw new IllegalArgumentException("Token inválido");
        }

        final String jwtToken = token.substring(7);
        final Token tokenEncontrado = tokenRepository.findByToken(jwtToken)
                        .orElseThrow(() -> new IllegalArgumentException("Tóken inválido"));

        tokenEncontrado.setExpirado(true);
        tokenEncontrado.setRemovido(true);

        tokenRepository.save(tokenEncontrado);
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}