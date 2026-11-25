package br.com.coleta.sistemas.agendaecapacidade.Publisher;

import br.com.coleta.sistemas.agendaecapacidade.Config.RabbitMQConfig;
import br.com.coleta.sistemas.agendaecapacidade.Entity.OutboxEvento;
import br.com.coleta.sistemas.agendaecapacidade.Repository.OutboxEventoRepository;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private final OutboxEventoRepository outboxEventoRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxPublisher(OutboxEventoRepository outboxEventoRepository, RabbitTemplate rabbitTemplate) {
        this.outboxEventoRepository = outboxEventoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publicarEventos() {
       outboxEventoRepository.liberarEventosTravados(LocalDateTime.now().minusMinutes(2));

        UUID token = UUID.randomUUID();

        outboxEventoRepository.reservarPendentes(token, 1);

        var eventos = outboxEventoRepository.findByProcessamentoId(token);
        for (OutboxEvento evento : eventos) {

            try {
                CorrelationData correlacao = new CorrelationData(new tools.jackson.databind.ObjectMapper().readTree(evento.getPayload()).get("messageId").asText());
                MessagePostProcessor processador = mensagem -> {
                    mensagem.getMessageProperties().setMessageId(new tools.jackson.databind.ObjectMapper().readTree(evento.getPayload()).get("messageId").asText());
                    return mensagem;
                };

                rabbitTemplate.convertAndSend(
                        "coleta.exchange", evento.getTipoEvento(),
                        evento.getPayload(), processador, correlacao);

                CorrelationData.Confirm confirmacao = correlacao.getFuture().get(10, TimeUnit.SECONDS);

                if (confirmacao == null || !confirmacao.ack()) {
                    throw new IllegalStateException("RabbitMQ não confirmou a publicação.");
                }

                if (correlacao.getReturned() != null) {
                    throw new IllegalStateException("Mensagem não foi encaminhada para uma fila.");
                }

                evento.marcarComoPublicado();
                outboxEventoRepository.save(evento);

                System.out.println("Evento publicado: " + evento.getId());

            }
            catch (Exception erro) {
                evento.registrarTentativa(erro.getMessage());
                outboxEventoRepository.save(evento);

                System.out.println("Falha ao publicar evento: " + erro.getMessage());
            }
        }
    }
}
