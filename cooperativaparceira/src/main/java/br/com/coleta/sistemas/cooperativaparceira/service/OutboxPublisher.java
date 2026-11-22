package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Entity.OutboxEvent;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import br.com.coleta.sistemas.cooperativaparceira.repository.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String EXCHANGE = "coleta.exchange";

    private final OutBoxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publicarPendentes() {

        List<OutboxEvent> pendentes =
                outboxEventRepository.findByStatusOrderByCriadoEmAsc(
                        StatusOutbox.PENDENTE,
                        PageRequest.of(0, 50)
                );

        for (OutboxEvent evento : pendentes) {
            publicar(evento);
        }
    }

    private void publicar(OutboxEvent evento) {

        String routingKey = obterRoutingKey(evento.getTipoEvento());

        Message mensagem = MessageBuilder
                .withBody(evento.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(evento.getMessageId().toString())
                .setCorrelationId(evento.getCorrelationId().toString())
                .build();

        CorrelationData correlationData =
                new CorrelationData(evento.getMessageId().toString());

        try {
            rabbitTemplate.send(
                    EXCHANGE,
                    routingKey,
                    mensagem,
                    correlationData
            );

            CorrelationData.Confirm confirmacao =
                    correlationData.getFuture().get(10, TimeUnit.SECONDS);

            if (!confirmacao.isAck()) {
                log.warn(
                        "Publicação não confirmada. messageId={}",
                        evento.getMessageId()
                );
                return;
            }

            if (correlationData.getReturned() != null) {
                log.warn(
                        "Mensagem sem rota válida. messageId={}",
                        evento.getMessageId()
                );
                return;
            }

            evento.setStatus(StatusOutbox.PUBLICADO);
            evento.setPublicadoEm(Instant.now());

            outboxEventRepository.save(evento);

            log.info(
                    "Evento publicado. tipo={}, messageId={}",
                    evento.getTipoEvento(),
                    evento.getMessageId()
            );

        } catch (Exception e) {
            log.error(
                    "Falha ao publicar evento {}",
                    evento.getMessageId(),
                    e
            );
        }
    }

    private String obterRoutingKey(String tipoEvento) {

        return switch (tipoEvento) {
            case "CooperativaAceitou" -> "cooperativa.aceitou";
            case "CooperativaRecusou" -> "cooperativa.recusou";
            case "CompromissoCancelado" -> "cooperativa.compromisso-cancelado";
            default -> throw new IllegalArgumentException(
                    "Tipo de evento desconhecido: " + tipoEvento
            );
        };
    }
}