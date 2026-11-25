package br.com.coleta.sistemas.solicitacoes.Publisher;

import br.com.coleta.sistemas.solicitacoes.Config.RabbitMQConfig;
import br.com.coleta.sistemas.solicitacoes.Entity.OutboxEvento;
import br.com.coleta.sistemas.solicitacoes.Repository.OutboxEventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int LIMITE_POR_CICLO = 50;

    private final OutboxEventoRepository outboxEventoRepository;
    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 500)
    public void publicarPendentes() {

        outboxEventoRepository.liberarEventosTravados(
                LocalDateTime.now().minusMinutes(2)
        );

        for (int i = 0; i < LIMITE_POR_CICLO; i++) {

            UUID token = UUID.randomUUID();

            int reservados =
                    outboxEventoRepository.reservarPendentes(token, 1);

            if (reservados == 0) {
                break;
            }

            var eventos =
                    outboxEventoRepository.findByProcessamentoId(token);

            if (eventos.isEmpty()) {
                break;
            }

            if (!publicar(eventos.getFirst(), token)) {
                break;
            }
        }
    }

    private boolean publicar(OutboxEvento evento, UUID token) {

        try {

            var payload = objectMapper.readTree(evento.getPayload());

            String messageId = payload.get("messageId").asText();
            String correlationId = payload.get("correlationId").asText();

            CorrelationData correlacao =
                    new CorrelationData(messageId);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_SOLICITACOES,
                    evento.getTipoEvento(),
                    evento.getPayload(),
                    mensagem -> {

                        var propriedades =
                                mensagem.getMessageProperties();

                        propriedades.setDeliveryMode(
                                MessageDeliveryMode.PERSISTENT
                        );

                        propriedades.setContentType("application/json");
                        propriedades.setMessageId(messageId);
                        propriedades.setCorrelationId(correlationId);

                        return mensagem;
                    },
                    correlacao
            );

            var confirmacao =
                    correlacao.getFuture().get(10, TimeUnit.SECONDS);

            if (!confirmacao.ack()) {
                throw new IllegalStateException(
                        "RabbitMQ não confirmou: " + confirmacao.reason()
                );
            }

            if (correlacao.getReturned() != null) {
                throw new IllegalStateException(
                        "Mensagem sem rota válida."
                );
            }

            evento.marcarComoPublicado();

            int atualizados = finalizar(evento, token);

            if (atualizados != 1) {

                log.warn(
                        "Publicação confirmada, mas token perdido. messageId={}",
                        messageId
                );

                return false;
            }

            log.info(
                    "Evento publicado. tipo={}, messageId={}",
                    evento.getTipoEvento(),
                    messageId
            );

            return true;

        } catch (Exception erro) {

            try {

                evento.registrarFalha(erro.getMessage());
                evento.marcarComoPendente();

                if (finalizar(evento, token) != 1) {
                    log.warn(
                            "Não foi possível liberar evento. id={}",
                            evento.getId()
                    );
                }

            } catch (Exception erroLiberacao) {

                log.error(
                        "Falha ao liberar evento {}",
                        evento.getId(),
                        erroLiberacao
                );
            }

            log.error(
                    "Falha ao publicar evento {}",
                    evento.getId(),
                    erro
            );

            if (erro instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            return false;
        }
    }

    private int finalizar(OutboxEvento evento, UUID token) {

        return outboxEventoRepository.finalizarComToken(
                evento.getId(),
                token,
                evento.getStatus(),
                evento.getPublicadoEm(),
                evento.getTentativas(),
                evento.getUltimoErro()
        );
    }
}