package br.com.coleta.sistemas.cooperativaparceira.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SOLICITACAO_LOCK")
public class SolicitacaoLock {

    @Id
    @Column(name = "SOLICITACAO_ID", nullable = false)
    private UUID solicitacaoId;
}