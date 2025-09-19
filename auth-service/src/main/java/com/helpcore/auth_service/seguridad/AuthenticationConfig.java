package com.helpcore.auth_service.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.helpcore.auth_service.entidades.Usuario;
import com.helpcore.auth_service.repositorios.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthenticationConfig {
    
    private final UsuarioRepository usuarioRepository;

    @Bean
    public UserDetailsService userDetailsService(){
        return detalleUsuario -> {
            final Usuario usuario_ = usuarioRepository.findByNombreUsuario(detalleUsuario).orElseThrow(
                () -> new UsernameNotFoundException("Usuario no encontrado"));

            return org.springframework.security.core.userdetails.User.builder()
                    .username(usuario_.getNombreUsuario())
                    .password(usuario_.getContrasena())
                    .build();
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}