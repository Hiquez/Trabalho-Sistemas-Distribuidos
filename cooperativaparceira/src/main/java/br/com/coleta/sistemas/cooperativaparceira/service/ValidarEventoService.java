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

        if (evento == null || evento.solicitacaoId() == null) {
            throw new IllegalArgumentException("solicitacaoId obrigatorio");
        }
    }
}
