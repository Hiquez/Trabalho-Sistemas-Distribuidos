
package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.EventEnvelope;
import br.com.coleta.sistemas.cooperativaparceira.Entity.OutboxEvent;
import br.com.coleta.sistemas.cooperativaparceira.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutBoxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(
            UUID correlationId,
            String tipoEvento,
            Object payload
    ) {
        try {
            UUID messageId = UUID.randomUUID();

            EventEnvelope<Object> envelope = new EventEnvelope<>(
                    messageId,
                    correlationId,
                    tipoEvento,
                    1,
                    Instant.now(),
                    payload
            );

            String json = objectMapper.writeValueAsString(envelope);

            OutboxEvent outbox = new OutboxEvent();

            outbox.setId(UUID.randomUUID());
            outbox.setMessageId(messageId);
            outbox.setCorrelationId(correlationId);
            outbox.setTipoEvento(tipoEvento);
            outbox.setPayload(json);
            outbox.setStatus(StatusOutbox.PENDENTE);
            outbox.setCriadoEm(Instant.now());

            outboxEventRepository.save(outbox);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao serializar evento: " + tipoEvento,
                    e
            );
        }
    }
}
