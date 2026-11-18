
package br.com.coleta.sistemas.cooperativaparceira.Entity;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusCompromisso;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "COMPROMISSO",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_COMPROMISSO_SOLICITACAO",
                columnNames = "SOLICITACAO_ID"
        )
)
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_COMPROMISSO")
    private UUID idCompromisso;

    @Column(name = "SOLICITACAO_ID", nullable = false)
    private UUID solicitacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private StatusCompromisso status;

    @Column(name = "CRIADO_EM", nullable = false)
    private Instant criadoEm;

    @Column(name = "ATUALIZADO_EM", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "CANCELADO_EM")
    private Instant canceladoEm;

    @Version
    @Column(name = "VERSAO")
    private Long versao;

    @Column(name = "RESERVA_ID", nullable = false)
    private UUID reservaId;
}
