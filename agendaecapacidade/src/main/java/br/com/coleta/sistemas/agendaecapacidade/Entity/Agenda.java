package br.com.coleta.sistemas.agendaecapacidade.Entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "agendas")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "solicitacao_id",nullable = false, unique = true)
    private UUID solicitacaoId;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgendaReserva status;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public Agenda() {

    }

    public void efetivar() {
        this.status = AgendaReserva.EFETIVADA;
    }

    public void liberar() {
        this.status = AgendaReserva.LIBERADA;
    }

    public Agenda(UUID solicitacaoId, String bairro, LocalDate dataColeta) {
        this.solicitacaoId = solicitacaoId;
        this.bairro = bairro;
        this.dataColeta = dataColeta;
        this.status = AgendaReserva.RESERVADA;
        this.criadoEm = LocalDateTime.now();
    }
}
