package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ValidarEventoServiceTest {
    private final ValidarEventoService service = new ValidarEventoService();

    @Test
    void aceitaEnvelopeDeSolicitacaoValido() {
        var evento = new SolicitarAceiteCooperativaEvent(UUID.randomUUID(), UUID.randomUUID(),
                "ELETRONICOS", BigDecimal.ONE, Instant.now());
        var envelope = new EventEnvelope<>(UUID.randomUUID(), UUID.randomUUID(),
                "SolicitarAceiteCooperativa", 1, Instant.now(), evento);
        assertDoesNotThrow(() -> service.validarSolicitacaoAceite(envelope));
    }

    @Test
    void rejeitaSolicitacaoSemReserva() {
        var evento = new SolicitarAceiteCooperativaEvent(UUID.randomUUID(), null,
                "ELETRONICOS", BigDecimal.ONE, Instant.now());
        var envelope = new EventEnvelope<>(UUID.randomUUID(), UUID.randomUUID(),
                "SolicitarAceiteCooperativa", 1, Instant.now(), evento);
        assertThrows(IllegalArgumentException.class, () -> service.validarSolicitacaoAceite(envelope));
    }

    @Test
    void rejeitaCancelamentoComTipoIncorreto() {
        var envelope = new EventEnvelope<>(UUID.randomUUID(), UUID.randomUUID(),
                "TipoInvalido", 1, Instant.now(), new CancelarCompromissoEvent(UUID.randomUUID(), "PRAZO_EXPIRADO"));
        assertThrows(IllegalArgumentException.class, () -> service.validarCancelamento(envelope));
    }

    @Test
    void aceitaCancelamentoValido() {
        var envelope = new EventEnvelope<>(UUID.randomUUID(), UUID.randomUUID(),
                "CancelarCompromisso", 1, Instant.now(), new CancelarCompromissoEvent(UUID.randomUUID(), "PRAZO_EXPIRADO"));
        assertDoesNotThrow(() -> service.validarCancelamento(envelope));
    }
}
