package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Entity.OutboxEvent;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import br.com.coleta.sistemas.cooperativaparceira.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxReservaService {
    private final OutBoxEventRepository repository;

    // Snapshot imutavel: nenhuma entidade JPA atravessa a transacao de reserva.
    public record EventoReservado(UUID id, UUID token, UUID messageId,
                                  UUID correlationId, String tipoEvento, String payload) {}

    @Transactional
    public Optional<EventoReservado> reservarProximo() {
        Instant agora = Instant.now();
        List<UUID> candidatos = repository.buscarCandidatos(
                StatusOutbox.PENDENTE, StatusOutbox.PROCESSANDO, agora, PageRequest.of(0, 20));
        for (UUID id : candidatos) {
            // Ler antes do UPDATE evita retornar estado obsoleto do contexto de persistencia.
            Optional<OutboxEvent> encontrado = repository.findById(id);
            if (encontrado.isEmpty()) continue;
            OutboxEvent evento = encontrado.get();
            UUID token = UUID.randomUUID();
            int alterados = repository.tentarReservar(id, StatusOutbox.PENDENTE,
                    StatusOutbox.PROCESSANDO, agora, agora.plus(60, ChronoUnit.SECONDS), token);
            if (alterados == 1) {
                return Optional.of(new EventoReservado(id, token, evento.getMessageId(),
                        evento.getCorrelationId(), evento.getTipoEvento(), evento.getPayload()));
            }
        }
        return Optional.empty();
    }

    @Transactional
    public boolean confirmar(UUID id, UUID token) {
        return repository.confirmarPublicacao(id, token, StatusOutbox.PROCESSANDO,
                StatusOutbox.PUBLICADO, Instant.now()) == 1;
    }

    @Transactional
    public boolean liberar(UUID id, UUID token) {
        return repository.liberarReserva(id, token, StatusOutbox.PROCESSANDO,
                StatusOutbox.PENDENTE) == 1;
    }
}
