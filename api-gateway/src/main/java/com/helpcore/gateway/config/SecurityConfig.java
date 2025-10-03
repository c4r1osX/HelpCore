package com.helpcore.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import com.helpcore.gateway.filter.AuthenticationFilter;

// * - Definir qué endpoints requieren autenticación
// * - Configurar el filtro de validación JWT
// * - Integrar CORS con security
// * - Manejar excepciones de seguridad
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                // NO CONFIGURAR CORS AQUÍ - Se maneja en Gateway

                .authorizeExchange(exchanges -> exchanges
                        // RUTAS PÚBLICAS
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // PERMITIR OPTIONS
                        .pathMatchers(HttpMethod.POST, "/api/ticket/crear-invitado").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/categoria-ticket/listar").permitAll()
                        // Health checks y métricas públicas
                        .pathMatchers(HttpMethod.GET, "/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Fallback endpoints
                        .pathMatchers("/fallback/**").permitAll()

                        // RUTAS QUE REQUIEREN AUTENTICACIÓN
                        .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll() // pa probar refresh
                        .pathMatchers("/api/metrics").authenticated()
                        .pathMatchers("/actuator/**").authenticated()

                        // CUALQUIER OTRA RUTA
                        .anyExchange().authenticated()
                )

                // SOLO EL FILTRO DE AUTENTICACIÓN
                .addFilterAfter(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // EXCEPCIONES
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(
                                    org.springframework.http.HttpStatus.UNAUTHORIZED
                            );
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String body = """
                        {
                            "error": "Unauthorized",
                            "message": "JWT token is missing or invalid",
                            "code": 401,
                            "timestamp": "%s"
                        }
                        """.formatted(java.time.Instant.now().toString());

                            org.springframework.core.io.buffer.DataBuffer buffer =
                                    exchange.getResponse().bufferFactory().wrap(body.getBytes());

                            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
                        })

                        .accessDeniedHandler((exchange, denied) -> {
                            exchange.getResponse().setStatusCode(
                                    org.springframework.http.HttpStatus.FORBIDDEN
                            );
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String body = """
                        {
                            "error": "Forbidden",
                            "message": "Insufficient permissions for this resource",
                            "code": 403,
                            "timestamp": "%s"
                        }
                        """.formatted(java.time.Instant.now().toString());

                            org.springframework.core.io.buffer.DataBuffer buffer =
                                    exchange.getResponse().bufferFactory().wrap(body.getBytes());

                            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
                        })
                )

                .build();
    }
}