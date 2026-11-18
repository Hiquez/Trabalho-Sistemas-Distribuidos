
package br.com.coleta.sistemas.cooperativaparceira.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "CANCELAMENTO_ANTECIPADO",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_CANCELAMENTO_SOLICITACAO",
                columnNames = "SOLICITACAO_ID"
        )
)
public class CancelamentoAntecipado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "SOLICITACAO_ID", nullable = false)
    private UUID solicitacaoId;

    @Column(name = "CORRELATION_ID", nullable = false)
    private UUID correlationId;

    @Column(name = "CRIADO_EM", nullable = false)
    private Instant criadoEm;
}
