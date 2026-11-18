package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.time.Instant;
import java.util.UUID;

public record CooperativaRecusouEvent(
        UUID solicitacaoId,
        UUID compromissoId,
        String motivoRecusa,
        Instant dataRecusa
) {
}
