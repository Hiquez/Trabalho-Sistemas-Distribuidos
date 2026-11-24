package br.com.coleta.sistemas.solicitacoes.Repository;

import br.com.coleta.sistemas.solicitacoes.Entity.OutboxEvento;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, UUID> {

    List<OutboxEvento> findByStatusOrderByCriadoEmAsc(String status);
    List<OutboxEvento> findByProcessamentoId(UUID processamentoId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE outbox_eventos
        SET status = 'PROCESSANDO',
            processamento_id = :token,
            processando_em = CURRENT_TIMESTAMP
        WHERE id IN (
            SELECT id
            FROM outbox_eventos
            WHERE status = 'PENDENTE'
            ORDER BY criado_em
            LIMIT :limite
            FOR UPDATE SKIP LOCKED
        )
        """, nativeQuery = true)

    int reservarPendentes(@Param("token") UUID token, @Param("limite") int limite);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE outbox_eventos
        SET status = 'PENDENTE',
            processamento_id = NULL,
            processando_em = NULL
        WHERE status = 'PROCESSANDO'
          AND processando_em < :limite
        """, nativeQuery = true)

    int liberarEventosTravados(@Param("limite") LocalDateTime limite);
}
