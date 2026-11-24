package br.com.coleta.sistemas.agendaecapacidade.Repository;

import br.com.coleta.sistemas.agendaecapacidade.Entity.CapacidadeAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CapacidadeAgendaRepository extends JpaRepository<CapacidadeAgenda, UUID> {

    Optional<CapacidadeAgenda> findByBairroAndDataColeta(String bairro, LocalDate dataColeta);

    @Modifying
    @Query("""
        UPDATE CapacidadeAgenda c
        SET c.vagasReservadas = c.vagasReservadas + 1
        WHERE c.bairro = :bairro
          AND c.dataColeta = :dataColeta
          AND c.vagasReservadas < c.capacidadeTotal
    """)

    int reservarVaga(@Param("bairro") String bairro, @Param("dataColeta") LocalDate dataColeta);

// liberar uma vaga
    @Modifying
    @Query("""
    UPDATE CapacidadeAgenda c
    SET c.vagasReservadas = c.vagasReservadas - 1
    WHERE c.bairro = :bairro
      AND c.dataColeta = :dataColeta
      AND c.vagasReservadas > 0
""")

    int liberarVaga(@Param("bairro") String bairro, @Param("dataColeta") LocalDate dataColeta);

}
