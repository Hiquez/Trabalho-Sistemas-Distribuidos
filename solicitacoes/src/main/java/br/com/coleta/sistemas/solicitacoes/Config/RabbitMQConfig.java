package br.com.coleta.sistemas.solicitacoes.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_SOLICITACAO_CRIADA = "solicitacao.criada";
    public static final String EXCHANGE_SOLICITACOES = "solicitacoes.exchange";
    public static final String ROUTING_KEY_SOLICITACAO_CRIADA = "solicitacao.criada";
    public static final String FILA_RESULTADO_RESERVA = "reserva.resultado";

    @Bean
    public Queue filaSolicitacaoCriada() {
        return new Queue(FILA_SOLICITACAO_CRIADA, true);
    }

    @Bean
    public DirectExchange exchangeSolicitacoes() {
        return new DirectExchange(EXCHANGE_SOLICITACOES, true, false);
    }

    @Bean
    public Binding bindingSolicitacaoCriada() {
        return BindingBuilder
                .bind(filaSolicitacaoCriada())
                .to(exchangeSolicitacoes())
                .with(ROUTING_KEY_SOLICITACAO_CRIADA);
    }

    @Bean
    public Queue filaResultadoReserva() {
        return new Queue(FILA_RESULTADO_RESERVA, true);
    }
}
