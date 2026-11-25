package br.com.coleta.sistemas.agendaecapacidade.Entity;

import br.com.coleta.sistemas.agendaecapacidade.Repository.OutboxEventoRepository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_eventos")
@Getter
@NoArgsConstructor
public class OutboxEvento {

    @Id
    private UUID id;

    @Column(name = "agregado_id", nullable = false)
    private UUID agregadoId;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @jakarta.persistence.Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String payload;

    @Column(nullable = false)
    private String status = "PENDENTE";

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "publicado_em")
    private LocalDateTime publicadoEm;

    @Column(nullable = false)
    private int tentativas = 0;

    @jakarta.persistence.Lob
    @Column(name = "ultimo_erro", columnDefinition = "CLOB")
    private String ultimoErro;

    @Column(name = "processamento_id")
    private UUID processamentoId;

    @Column(name = "processando_em")
    private LocalDateTime processandoEm;

    public OutboxEvento(UUID agregadoId, String tipoEvento, String payload) {
        this.id = UUID.randomUUID();
        this.agregadoId = agregadoId;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.status = "PENDENTE";
        this.criadoEm = LocalDateTime.now();
        this.tentativas = 0;
    }

    public void marcarComoPublicado() {
        this.status = "PUBLICADO";
        this.publicadoEm = LocalDateTime.now();
        this.ultimoErro = null;
        this.processamentoId = null;
        this.processandoEm = null;
    }

    public void registrarTentativa(String erro) {
        this.tentativas++;
        this.ultimoErro = erro;
        this.status = "PENDENTE";
        this.publicadoEm = null;
        this.processamentoId = null;
        this.processandoEm = null;
    }

    public boolean estaPendente() {
        return "PENDENTE".equals(this.status);
    }
}