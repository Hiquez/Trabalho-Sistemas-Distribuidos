package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.EventEnvelope;
import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.SolicitarAceiteCooperativaEvent;
import org.springframework.stereotype.Service;

@Service
public class ValidarEventoService {

    public void validarSolicitacaoAceite(EventEnvelope<SolicitarAceiteCooperativaEvent> envelope){

        if (envelope == null) {
            throw new IllegalArgumentException("Evento obrigatorio");
        }

        if (envelope.messageId() == null) {
            throw new IllegalArgumentException("messageId obrigatorio");
        }

        if (envelope.correlationId() == null) {
            throw new IllegalArgumentException("correlationId obrigatorio");
        }

        if (!Integer.valueOf(1).equals(envelope.version())) {
            throw new IllegalArgumentException("Versao nao suportada");
        }

        if (!"SolicitarAceiteCooperativa".equals(envelope.type())) {
            throw new IllegalArgumentException("Tipo de evento invalido");
        }

        if (envelope.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt obrigatorio");
        }

        SolicitarAceiteCooperativaEvent evento = envelope.payload();

        if (evento == null || evento.solicitacaoId() == null || evento.reservaId() == null) {
            throw new IllegalArgumentException("solicitacaoId e reservaId obrigatorios");
        }
    }

    public void validarCancelamento(br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.EventEnvelope<br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.CancelarCompromissoEvent> envelope) {
        if (envelope == null || envelope.messageId() == null || envelope.correlationId() == null
                || envelope.occurredAt() == null || envelope.payload() == null
                || envelope.payload().solicitacaoId() == null
                || envelope.payload().motivoCancelamento() == null
                || envelope.payload().motivoCancelamento().isBlank()) {
            throw new IllegalArgumentException("Envelope de cancelamento invalido");
        }
        if (!Integer.valueOf(1).equals(envelope.version()) || !"CancelarCompromisso".equals(envelope.type())) {
            throw new IllegalArgumentException("Tipo ou versao de cancelamento invalido");
        }
    }
}
