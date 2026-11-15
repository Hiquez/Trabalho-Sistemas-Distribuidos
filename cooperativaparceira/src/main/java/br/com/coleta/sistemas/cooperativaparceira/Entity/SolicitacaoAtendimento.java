package br.com.coleta.sistemas.cooperativaparceira.Entity;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.ResultadoAtendimento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class SolicitacaoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idSolicitacaoAtendimento;
    private UUID idSolicitacao;
    private ResultadoAtendimento resultadoAtendimento;
    private Instant recebidoEm;
    private Instant processadoEm;
}
