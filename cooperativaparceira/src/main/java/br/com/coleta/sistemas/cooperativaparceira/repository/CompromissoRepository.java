package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.Compromisso;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusCompromisso;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompromissoRepository
        extends JpaRepository<Compromisso, UUID> {

    Optional<Compromisso> findByIdCompromisso(UUID uuid);

    Optional<Compromisso> findBySolicitacaoId(UUID uuid);

    List<Compromisso> findByStatusOrderByCriadoEmAsc(
            StatusCompromisso status,
            Pageable pageable
    );
}