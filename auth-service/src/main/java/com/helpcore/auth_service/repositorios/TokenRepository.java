package com.helpcore.auth_service.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.helpcore.auth_service.entidades.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    @Query("""
        SELECT t FROM tb_token t
        WHERE t.usuario.id = :id AND (t.expirado = false OR t.removido = false)
    """)
    List<Token> findAllValidTokensByUserId(@Param("id") Integer id);

    Optional<Token> findByToken(String token);

}
