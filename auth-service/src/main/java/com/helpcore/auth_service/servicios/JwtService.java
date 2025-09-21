package com.helpcore.auth_service.servicios;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.helpcore.auth_service.entidades.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@RefreshScope
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secretKey;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String generarToken(final Usuario usuario) {
        return buildToken(usuario, jwtExpiration);
    }

    public String generarRefreshToken(final Usuario usuario) {
        return buildToken(usuario, refreshExpiration);
    }

    public String extraerUsuario(final String token) {
        final Claims jwtToken = Jwts.parser()
                .setSigningKey(getSignInKey())
                .parseClaimsJws(token)
                .getBody();

        return jwtToken.getSubject();
    }

    public boolean validarToken(final String token, final Usuario usuario){
        final String nombreUsuario = extraerUsuario(token);
        return (nombreUsuario.equals(usuario.getNombreUsuario())) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(final String token){
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(final String token){
        final Claims jwtToken = Jwts.parser()
                .setSigningKey(getSignInKey())
                .parseClaimsJws(token)
                .getBody();

        return jwtToken.getExpiration();
    }

    private String buildToken(final Usuario usuario, final long expiration) {
        return Jwts.builder()
                .setId(usuario.getId().toString())
                .addClaims(Map.of("usuario", usuario.getNombreUsuario()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setSubject(usuario.getNombreUsuario())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
