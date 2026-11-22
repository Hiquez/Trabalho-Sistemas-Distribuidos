package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.SolicitacaoLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoLockRepository
        extends JpaRepository<SolicitacaoLock, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM SolicitacaoLock s
        WHERE s.solicitacaoId = :solicitacaoId
    """)
    Optional<SolicitacaoLock> bloquear(
            @Param("solicitacaoId") UUID solicitacaoId
    );

    @Modifying
    @Query(
            value = """
        INSERT INTO SOLICITACAO_LOCK (SOLICITACAO_ID)
        VALUES (:solicitacaoId)
        """,
            nativeQuery = true
    )
    void inserir(@Param("solicitacaoId") UUID solicitacaoId);
}