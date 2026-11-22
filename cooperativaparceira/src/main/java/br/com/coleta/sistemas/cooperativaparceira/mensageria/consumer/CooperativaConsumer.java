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

    @RabbitListener(
            queues = RabbitMQConfig.FILA_SOLICITAR_ACEITE
    )
    public void consumirSolicitarAceite(String mensagem) throws Exception {

        EventEnvelope<SolicitarAceiteCooperativaEvent> envelope =
                objectMapper.readValue(
                        mensagem,
                        new TypeReference<EventEnvelope<SolicitarAceiteCooperativaEvent>>() {}
                );

        UUID solicitacaoId = envelope.payload().solicitacaoId();

        log.info(
                "Recebido SolicitarAceiteCooperativa. messageId={}, solicitacaoId={}",
                envelope.messageId(),
                solicitacaoId
        );

        solicitacaoLockService.iniciarLock(solicitacaoId);

        UUID compromissoId = compromissoService.criarCompromisso(envelope);

        if (compromissoId == null) {
            log.info(
                    "Solicitação {} ignorada por cancelamento antecipado.",
                    solicitacaoId
            );
            return;
        }

        log.info(
                "Compromisso {} criado ou recuperado para solicitação {}.",
                compromissoId,
                solicitacaoId
        );

        // O processamento da simulação será acionado
        // por um mecanismo separado e recuperável.
    }

    @RabbitListener(
            queues = RabbitMQConfig.FILA_CANCELAR_COMPROMISSO
    )
    public void consumirCancelarCompromisso(String mensagem) throws Exception {

        EventEnvelope<CancelarCompromissoEvent> envelope =
                objectMapper.readValue(
                        mensagem,
                        new TypeReference<
                                EventEnvelope<CancelarCompromissoEvent>>() {}
                );

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