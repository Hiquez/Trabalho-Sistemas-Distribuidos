package br.com.coleta.sistemas.solicitacoes.Repository;

import br.com.coleta.sistemas.solicitacoes.Entity.Solicitacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoRespository extends JpaRepository<Solicitacao, UUID> {

    @Query("select s.id from Solicitacao s where s.prazo <= :agora and s.status = br.com.coleta.sistemas.solicitacoes.Entity.StatusSolicitacao.AGUARDANDO_COOPERATIVA")
    java.util.List<UUID> expiradas(@Param("agora") java.time.Instant agora);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Solicitacao s WHERE s.id = :id")
    Optional<Solicitacao> buscarPorIdComBloqueio(@Param("id") UUID id);
}
