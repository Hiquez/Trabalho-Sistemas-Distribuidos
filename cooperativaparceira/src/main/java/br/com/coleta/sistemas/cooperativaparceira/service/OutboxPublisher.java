
package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.service.OutboxReservaService.EventoReservado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String EXCHANGE = "coleta.exchange";
    private static final int LIMITE_POR_CICLO = 50;

    private final OutboxReservaService reservaService;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 500)
    public void publicarPendentes() {

        for (int i = 0; i < LIMITE_POR_CICLO; i++) {

            var reservado = reservaService.reservarProximo();

            if (reservado.isEmpty()) {
                break;
            }

            if (!publicar(reservado.get())) {
                // Nao reprocessar imediatamente o mesmo evento com falha.
                break;
            }
        }
    }

    private boolean publicar(EventoReservado evento) {
        UUID token = evento.token();

        try {

            String routingKey =
                    obterRoutingKey(evento.tipoEvento());

            Message mensagem = MessageBuilder
                    .withBody(
                            evento.payload()
                                    .getBytes(StandardCharsets.UTF_8)
                    )
                    .setContentType(
                            MessageProperties.CONTENT_TYPE_JSON
                    )
                    .setDeliveryMode(
                            MessageDeliveryMode.PERSISTENT
                    )
                    .setMessageId(
                            evento.messageId().toString()
                    )
                    .setCorrelationId(
                            evento.correlationId().toString()
                    )
                    .build();

            CorrelationData correlationData =
                    new CorrelationData(
                            evento.messageId().toString()
                    );

            rabbitTemplate.send(
                    EXCHANGE,
                    routingKey,
                    mensagem,
                    correlationData
            );

            CorrelationData.Confirm confirmacao =
                    correlationData.getFuture()
                            .get(10, TimeUnit.SECONDS);

            if (!confirmacao.ack()) {

                log.warn(
                        "Publicação não confirmada: {}",
                        evento.messageId()
                );

                reservaService.liberar(
                        evento.id(),
                        token
                );

                return false;
            }

            if (correlationData.getReturned() != null) {

                log.warn(
                        "Evento sem rota válida: {}",
                        evento.messageId()
                );

                reservaService.liberar(
                        evento.id(),
                        token
                );

                return false;
            }

            boolean confirmado = reservaService.confirmar(
                    evento.id(),
                    token
            );

            if (confirmado) {

                log.info(
                        "Evento publicado. tipo={}, messageId={}",
                        evento.tipoEvento(),
                        evento.messageId()
                );

            } else {

                log.warn(
                        "Publicação confirmada no RabbitMQ, "
                                + "mas reserva não pôde ser finalizada. "
                                + "messageId={}",
                        evento.messageId()
                );
            }
            return confirmado;

        } catch (Exception e) {

            log.error(
                    "Falha ao publicar evento {}",
                    evento.messageId(),
                    e
            );

            try {
                reservaService.liberar(
                        evento.id(),
                        token
                );
            } catch (Exception erroLiberacao) {
                log.error(
                        "Falha ao liberar reserva do evento {}",
                        evento.messageId(),
                        erroLiberacao
                );
            }

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return false;
        }
    }

    private String obterRoutingKey(String tipoEvento) {

        return switch (tipoEvento) {

            case "CooperativaAceitou" ->
                    "cooperativa.aceitou";

            case "CooperativaRecusou" ->
                    "cooperativa.recusou";

            case "CompromissoCancelado" ->
                    "cooperativa.compromisso-cancelado";

            default -> throw new IllegalArgumentException(
                    "Tipo de evento desconhecido: " + tipoEvento
            );
        };
    }
}
