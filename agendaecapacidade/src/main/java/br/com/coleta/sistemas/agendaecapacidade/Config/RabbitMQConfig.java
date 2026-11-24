
package br.com.coleta.sistemas.agendaecapacidade.Config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String FILA_SOLICITACAO_CRIADA = "solicitacao.criada";
    public static final String FILA_RESULTADO_RESERVA = "reserva.resultado";

    @Bean
    public Queue filaSolicitacaoCriada() {
        return QueueBuilder.durable(FILA_SOLICITACAO_CRIADA)
                .deadLetterExchange("")
                .deadLetterRoutingKey("solicitacao.criada.dlq")
                .build();
    }

    @Bean
    public Queue filaSolicitacaoCriadaDlq() {
        return new Queue("solicitacao.criada.dlq", true);
    }

    @Bean
    public Queue filaResultadoReserva() {
        return new Queue(FILA_RESULTADO_RESERVA, true);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);

        return factory;
    }
}
