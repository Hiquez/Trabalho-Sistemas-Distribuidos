package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.time.Instant;
import java.util.UUID;

public record CompromissoCanceladoEvent(
        UUID solicitacaoId,
        UUID compromissoId,
        String motivoCancelamento,
        Instant dataCancelamento
) {
}
