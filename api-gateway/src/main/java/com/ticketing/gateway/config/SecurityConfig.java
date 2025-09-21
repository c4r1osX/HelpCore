package com.ticketing.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import com.ticketing.gateway.filter.AuthenticationFilter;

// * - Definir qué endpoints requieren autenticación
// * - Configurar el filtro de validación JWT
// * - Integrar CORS con security
// * - Manejar excepciones de seguridad

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final CorsWebFilter corsWebFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter,
                          CorsWebFilter corsWebFilter) {
        this.authenticationFilter = authenticationFilter;
        this.corsWebFilter = corsWebFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                .cors(cors -> cors.configurationSource(exchange -> null))

                .authorizeExchange(exchanges -> exchanges
                        // RUTAS PÚBLICAS
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                        // Health checks y métricas públicas
                        .pathMatchers(HttpMethod.GET, "/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                        // Fallback endpoints
                        .pathMatchers("/fallback/**").permitAll()

                        // RUTAS QUE REQUIEREN AUTENTICACIÓN
                        .pathMatchers("/api/auth/refresh").authenticated()

                        // Métricas y admin endpoints (requieren autenticación)
                        .pathMatchers("/api/metrics").authenticated()
                        .pathMatchers("/actuator/**").authenticated()

                        // CUALQUIER OTRA RUTA
                        .anyExchange().authenticated()  // Por defecto, requiere autenticación
                )

                // FILTROS PERSONALIZADOS
                .addFilterBefore(corsWebFilter, SecurityWebFiltersOrder.CORS)  // CORS
                .addFilterAfter(authenticationFilter, SecurityWebFiltersOrder.CORS)  // JWT

                // EXCEPCIONES
                .exceptionHandling(exceptions -> exceptions
                        // Cuando no está autenticado (401)
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

                        // Cuando no tiene permisos (403)
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

    // DESABILITAR CONFIGURACION
    @Bean("developmentSecurityWebFilterChain")
    public SecurityWebFilterChain developmentSecurityWebFilterChain(ServerHttpSecurity http) {
        // profile "dev-no-security"
        // Útil para testing y desarrollo inicial
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(exchange -> null))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()  // Permitir todo en desarrollo
                )
                .build();
    }
}