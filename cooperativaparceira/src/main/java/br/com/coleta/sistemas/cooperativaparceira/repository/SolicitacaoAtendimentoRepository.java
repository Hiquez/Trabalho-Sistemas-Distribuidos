package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.SolicitacaoAtendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SolicitacaoAtendimentoRepository extends JpaRepository<SolicitacaoAtendimento, UUID> {
}
