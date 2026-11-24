package br.com.coleta.sistemas.agendaecapacidade.Service;

import br.com.coleta.sistemas.agendaecapacidade.Entity.OutboxEvento;
import br.com.coleta.sistemas.agendaecapacidade.Repository.OutboxEventoRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxEventoService {

    private final OutboxEventoRepository outboxEventoRepository;

    public OutboxEventoService(OutboxEventoRepository outboxEventoRepository) {
        this.outboxEventoRepository = outboxEventoRepository;
    }

    public void registrarEvento(UUID agregadoId, String tipoEvento, String payload) {
        OutboxEvento evento = new OutboxEvento(
                agregadoId,
                tipoEvento,
                payload
        );

        outboxEventoRepository.save(evento);
    }
}