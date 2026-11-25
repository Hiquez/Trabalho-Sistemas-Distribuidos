package br.com.coleta.sistemas.cooperativaparceira.mensageria.consumer;

import br.com.coleta.sistemas.cooperativaparceira.mensageria.config.RabbitMQConfig;
import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.*;
import br.com.coleta.sistemas.cooperativaparceira.service.CompromissoService;
import br.com.coleta.sistemas.cooperativaparceira.service.SolicitacaoLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CooperativaConsumer {

    private final ObjectMapper objectMapper;
    private final CompromissoService compromissoService;
    private final SolicitacaoLockService solicitacaoLockService;
    private final br.com.coleta.sistemas.cooperativaparceira.service.ValidarEventoService validarEventoService;

    @RabbitListener(
            queues = "${MQ_QUEUE_COOPERATIVA_SOLICITAR_ACEITE:cooperativa.solicitar-aceite}"
    )
    public void consumirSolicitarAceite(String mensagem) throws Exception {
        br.com.coleta.sistemas.cooperativaparceira.ContratoEvento.validar(objectMapper,mensagem,"SolicitarAceiteCooperativa");

        EventEnvelope<SolicitarAceiteCooperativaEvent> envelope =
                objectMapper.readValue(
                        mensagem,
                        new TypeReference<EventEnvelope<SolicitarAceiteCooperativaEvent>>() {}
                );

        validarEventoService.validarSolicitacaoAceite(envelope);
        UUID solicitacaoId = envelope.payload().solicitacaoId();

        log.info(
                "Recebido SolicitarAceiteCooperativa. messageId={}, solicitacaoId={}",
                envelope.messageId(),
                solicitacaoId
        );

        solicitacaoLockService.iniciarLock(solicitacaoId);

        UUID compromissoId = compromissoService.criarCompromisso(envelope);

        // O processamento da simulação será acionado
        // por um mecanismo separado e recuperável.
    }

    @RabbitListener(
            queues = "${MQ_QUEUE_COOPERATIVA_CANCELAR_COMPROMISSO:cooperativa.cancelar-compromisso}"
    )
    public void consumirCancelarCompromisso(String mensagem) throws Exception {
        br.com.coleta.sistemas.cooperativaparceira.ContratoEvento.validar(objectMapper,mensagem,"CancelarCompromisso");

        EventEnvelope<CancelarCompromissoEvent> envelope =
                objectMapper.readValue(
                        mensagem,
                        new TypeReference<
                                EventEnvelope<CancelarCompromissoEvent>>() {}
                );

        validarEventoService.validarCancelamento(envelope);
        UUID solicitacaoId = envelope.payload().solicitacaoId();

        log.info(
                "Recebido CancelarCompromisso. messageId={}, solicitacaoId={}",
                envelope.messageId(),
                solicitacaoId
        );

        solicitacaoLockService.iniciarLock(solicitacaoId);

        compromissoService.cancelarCompromisso(envelope);

        log.info(
                "Cancelamento processado para solicitação {}.",
                solicitacaoId
        );
    }
}