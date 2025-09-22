package com.helpcore.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import com.helpcore.gateway.filter.AuthenticationFilter;


// * - Define qué endpoints requieren autenticación
// * - Configura el filtro de validación JWT
// * - Integra CORS con security
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

                // Configuración de CORS
                .cors(cors -> cors.disable()) // CORS manejado por CorsWebFilter

                // Configuración de autorización de endpoints
                .authorizeExchange(exchanges -> exchanges
                        // RUTAS PÚBLICAS
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight CORS

                        // Health checks y documentación
                        .pathMatchers(HttpMethod.GET, "/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()

                        // Endpoints de fallback
                        .pathMatchers("/fallback/**").permitAll()

                        // RUTAS PROTEGIDAS
                        .pathMatchers("/api/auth/refresh").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyExchange().authenticated()
                )

                // FILTROS PERSONALIZADOS
                .addFilterBefore(corsWebFilter, SecurityWebFiltersOrder.CORS)
                .addFilterAfter(authenticationFilter, SecurityWebFiltersOrder.CORS)

                // EXCEPCIONES
                .exceptionHandling(exceptions -> exceptions
                        // 401 - No autenticado
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(
                                    org.springframework.http.HttpStatus.UNAUTHORIZED
                            );
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String body = """
                        {
                            "error": "Unauthorized",
                            "message": "Authentication required",
                            "code": 401,
                            "timestamp": "%s"
                        }
                        """.formatted(java.time.Instant.now().toString());

                            org.springframework.core.io.buffer.DataBuffer buffer =
                                    exchange.getResponse().bufferFactory().wrap(body.getBytes());

                            return exchange.getResponse().writeWith(reactor.core.publisher.Mono.just(buffer));
                        })

                        // 403 - Sin permisos
                        .accessDeniedHandler((exchange, denied) -> {
                            exchange.getResponse().setStatusCode(
                                    org.springframework.http.HttpStatus.FORBIDDEN
                            );
                            exchange.getResponse().getHeaders().add("Content-Type", "application/json");

                            String body = """
                        {
                            "error": "Forbidden",
                            "message": "Insufficient permissions",
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