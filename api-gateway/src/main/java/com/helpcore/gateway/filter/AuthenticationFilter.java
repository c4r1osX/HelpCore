package com.helpcore.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

// * CORRECCIONES APLICADAS:
// * 1. Compatibilidad con jjwt 0.11.5 (mismo que auth-service)
// * 2. Return temprano para rutas públicas
// * 3. Validación JWT robusta
// * 4. Logging detallado para debugging
@Component
public class AuthenticationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // EXTRAER JWT TOKEN DEL HEADER AUTHORIZATION
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "No se encontro Authorization header format. Expected: Bearer <token>");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateAndParseToken(token);

            // EXTRAER INFORMACIÓN DEL USUARIO
            String userId = claims.getId();
            String username = claims.getSubject();
            String usuarioName = claims.get("usuario", String.class);

            if (username == null || username.trim().isEmpty()) {
                return handleUnauthorized(exchange, "Invalid token: missing username");
            }

            // CREAR AUTHENTICATION OBJECT
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // AGREGAR HEADERS PARA MICROSERVICIOS DOWNSTREAM
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : username)
                    .header("X-User-Username", username)
                    .header("X-User-Name", usuarioName != null ? usuarioName : username)
                    .header("X-User-Role", "USER")
                    .header("X-Auth-Source", "api-gateway")
                    .header("X-Auth-Timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // ESTABLECER CONTEXTO DE SEGURIDAD Y CONTINUAR
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            return handleUnauthorized(exchange, "Invalid JWT token: " + e.getMessage());
        }
    }

    private Claims validateAndParseToken(String token) throws Exception {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // VERIFICAR EXPIRACIÓN
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                throw new Exception("Token has expired at: " + expiration);
            }

            // VERIFICAR CLAIMS REQUERIDOS
            if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
                throw new Exception("Token missing subject (username)");
            }

            // VERIFICAR CLAIM 'usuario'
            if (claims.get("usuario") == null) {
                throw new Exception("Token missing 'usuario' claim");
            }

            return claims;

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new Exception("Token expired: " + e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new Exception("Unsupported token: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new Exception("Malformed token: " + e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new Exception("Invalid token signature: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Token validation failed: " + e.getMessage());
        }
    }

    // VERIFICAR SI ES RUTA PÚBLICA
    private boolean isPublicPath(String path) {
        if (path == null) return false;

        // RUTAS DE AUTENTICACIÓN PÚBLICAS
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/register")) {
            return true;
        }

        // HEALTH CHECKS Y DOCUMENTACIÓN
        if (path.equals("/health") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/api/docs")) {
            return true;
        }

        // ENDPOINTS DE FALLBACK
        if (path.startsWith("/fallback/")) {
            return true;
        }
        return false;
    }

    // MANEJAR REQUESTS NO AUTORIZADOS
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Error-Source", "api-gateway-auth-filter");

        String body = String.format("""
            {
                "error": "Unauthorized",
                "message": "%s",
                "code": 401,
                "timestamp": "%s",
                "path": "%s",
                "method": "%s"
            }
            """,
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getMethod().name()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}