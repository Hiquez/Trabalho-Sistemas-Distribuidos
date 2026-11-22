
package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.OutboxEvent;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutBoxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
        SELECT o.id
        FROM OutboxEvent o
        WHERE o.status = :pendente
           OR (
               o.status = :processando
               AND o.reservadoAte < :agora
           )
        ORDER BY o.criadoEm ASC
    """)
    List<UUID> buscarCandidatos(
            @Param("pendente") StatusOutbox pendente,
            @Param("processando") StatusOutbox processando,
            @Param("agora") Instant agora,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE OutboxEvent o
        SET o.status = :processando,
            o.reservadoAte = :reservadoAte,
            o.tokenReserva = :token
        WHERE o.id = :id
          AND (
              o.status = :pendente
              OR (
                  o.status = :processando
                  AND o.reservadoAte < :agora
              )
          )
    """)
    int tentarReservar(
            @Param("id") UUID id,
            @Param("pendente") StatusOutbox pendente,
            @Param("processando") StatusOutbox processando,
            @Param("agora") Instant agora,
            @Param("reservadoAte") Instant reservadoAte,
            @Param("token") UUID token
    );

    @Modifying
    @Query("""
        UPDATE OutboxEvent o
        SET o.status = :publicado,
            o.publicadoEm = :agora,
            o.reservadoAte = null,
            o.tokenReserva = null
        WHERE o.id = :id
          AND o.status = :processando
          AND o.tokenReserva = :token
    """)
    int confirmarPublicacao(
            @Param("id") UUID id,
            @Param("token") UUID token,
            @Param("processando") StatusOutbox processando,
            @Param("publicado") StatusOutbox publicado,
            @Param("agora") Instant agora
    );

    @Modifying
    @Query("""
        UPDATE OutboxEvent o
        SET o.status = :pendente,
            o.reservadoAte = null,
            o.tokenReserva = null
        WHERE o.id = :id
          AND o.status = :processando
          AND o.tokenReserva = :token
    """)
    int liberarReserva(
            @Param("id") UUID id,
            @Param("token") UUID token,
            @Param("processando") StatusOutbox processando,
            @Param("pendente") StatusOutbox pendente
    );
}
