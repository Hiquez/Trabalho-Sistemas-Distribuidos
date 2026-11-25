package br.com.coleta.sistemas.solicitacoes.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_eventos")
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
    private String status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "publicado_em")
    private LocalDateTime publicadoEm;

    @Column(nullable = false)
    private Integer tentativas;

    @jakarta.persistence.Lob
    @Column(name = "ultimo_erro", columnDefinition = "CLOB")
    private String ultimoErro;

    @Column(name = "processamento_id")
    private UUID processamentoId;

    @Column(name = "processando_em")
    private LocalDateTime processandoEm;

    protected OutboxEvento() {

    }

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

    public  void registrarFalha(String erro) {
        this.tentativas++;
        this.ultimoErro = erro;
    }

    public void marcarComoPendente() {
        this.status = "PENDENTE";
        this.publicadoEm = null;
        this.processamentoId = null;
        this.processandoEm = null;
    }
}
