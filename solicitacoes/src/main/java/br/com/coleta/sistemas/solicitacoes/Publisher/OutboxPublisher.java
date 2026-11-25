package br.com.coleta.sistemas.solicitacoes.Publisher;

import br.com.coleta.sistemas.solicitacoes.Config.RabbitMQConfig;
import br.com.coleta.sistemas.solicitacoes.Entity.OutboxEvento;
import br.com.coleta.sistemas.solicitacoes.Repository.OutboxEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventoRepository outboxEventoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publicarPendentes() {

        outboxEventoRepository.liberarEventosTravados(LocalDateTime.now().minusMinutes(2));

        UUID token = UUID.randomUUID();

        outboxEventoRepository.reservarPendentes(token, 1);

        var eventos = outboxEventoRepository.findByProcessamentoId(token);

        for (OutboxEvento evento : eventos) {
            try {
                CorrelationData correlacao = new CorrelationData(new tools.jackson.databind.ObjectMapper().readTree(evento.getPayload()).get("messageId").asText());

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_SOLICITACOES,
                        evento.getTipoEvento(),
                        evento.getPayload(),
                        mensagem -> {
                            mensagem.getMessageProperties().setMessageId(
                                    new tools.jackson.databind.ObjectMapper().readTree(evento.getPayload()).get("messageId").asText());
                            return mensagem;
                        },
                        correlacao
                );

                var confirmacao = correlacao.getFuture().get(10, TimeUnit.SECONDS);

                if (!confirmacao.ack()) {
                    throw new IllegalStateException("RabbitMQ não confirmou a mensagem: " + confirmacao.reason());
                }

                if (correlacao.getReturned() != null) {
                    throw new IllegalStateException("A mensagem não foi encaminhada para nenhuma fila.");
                }

                evento.marcarComoPublicado();
                outboxEventoRepository.save(evento);
            }
            catch (Exception erro) {

                evento.registrarFalha(erro.getMessage());
                evento.marcarComoPendente();

                outboxEventoRepository.save(evento);

                if (erro instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
