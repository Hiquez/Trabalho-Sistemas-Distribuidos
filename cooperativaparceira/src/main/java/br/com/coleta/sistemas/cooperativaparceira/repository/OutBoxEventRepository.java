package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.OutboxEvent;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCriadoEmAsc(
            StatusOutbox status,
            Pageable pageable
    );
}