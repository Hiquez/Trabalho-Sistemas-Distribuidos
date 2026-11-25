package br.com.coleta.sistemas.solicitacoes.Entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    private UUID reservaId;
    private java.time.Instant prazo;
    public void reservar(UUID id, java.time.Instant limite){reservaId=id; prazo=limite; status=StatusSolicitacao.AGUARDANDO_COOPERATIVA;}
    public void confirmar(){status=StatusSolicitacao.CONFIRMADA;}
    public void cancelar(){status=StatusSolicitacao.CANCELADA;}
    public Solicitacao() {

    }

    public Solicitacao(String bairro, LocalDate dataColeta) {
        this.bairro = bairro;
        this.dataColeta = dataColeta;
        this.status = StatusSolicitacao.PENDENTE;
        this.criadoEm = LocalDateTime.now();
    }

    public boolean atualizarStatus(StatusSolicitacao novoStatus) {

        if (novoStatus == null) {
            throw new IllegalArgumentException("Status inválido.");
        }

        if (this.status == StatusSolicitacao.CANCELADA ||this.status == StatusSolicitacao.CONFIRMADA) {
            return false;
        }

        if (this.status != StatusSolicitacao.PENDENTE && this.status != StatusSolicitacao.AGUARDANDO_RESERVA) {
            return false;
        }

        if (novoStatus != StatusSolicitacao.AGUARDANDO_COOPERATIVA && novoStatus != StatusSolicitacao.CANCELADA) {
            throw new IllegalArgumentException("Transição de status não permitida.");
        }

        this.status = novoStatus;
        return true;
    }
}