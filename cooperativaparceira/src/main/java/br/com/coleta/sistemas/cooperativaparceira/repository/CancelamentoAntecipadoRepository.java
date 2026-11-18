package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.CancelamentoAntecipado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CancelamentoAntecipadoRepository extends JpaRepository<CancelamentoAntecipado, UUID> {
    boolean existsBySolicitacaoId(UUID solicitacaoId);
}
