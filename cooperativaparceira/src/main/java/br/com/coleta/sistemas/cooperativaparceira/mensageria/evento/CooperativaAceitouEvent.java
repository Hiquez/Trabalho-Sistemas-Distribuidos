package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.time.Instant;
import java.util.UUID;

public record CooperativaAceitouEvent(
        UUID solicitacaoId,
        UUID compromissoId,
        UUID reservaId,
        Instant dataAceite
) {
}
