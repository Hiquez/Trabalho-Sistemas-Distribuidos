package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SolicitarAceiteCooperativaEvent(
        UUID solicitacaoId,
        UUID reservaId,
        String categoriaResiduo,
        BigDecimal quantidadeEstimada,
        Instant dataAgendada
) {
}
