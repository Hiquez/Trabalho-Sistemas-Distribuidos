package br.com.coleta.sistemas.agendaecapacidade.Repository;

import br.com.coleta.sistemas.agendaecapacidade.Entity.Agenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgendaRepository extends JpaRepository<Agenda, UUID> {

    Optional<Agenda> findBySolicitacaoId(UUID solicitacaoId);
    Optional<Agenda> findByBairroAndDataColeta(String bairro, java.time.LocalDate dataColeta);
}
