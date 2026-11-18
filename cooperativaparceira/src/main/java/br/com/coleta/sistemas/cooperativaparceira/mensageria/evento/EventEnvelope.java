package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID messageId,
        UUID correlationId,
        String type,
        Integer version,
        Instant occurredAt,
        T payload
) {}