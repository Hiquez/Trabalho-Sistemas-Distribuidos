package br.com.coleta.sistemas.agendaecapacidade.Config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_SOLICITACAO_CRIADA = "solicitacao.criada";
    public static final String FILA_RESULTADO_RESERVA = "reserva.resultado";

    @Bean
    public Queue filaSolicitacaoCriada() {
        return new Queue(FILA_SOLICITACAO_CRIADA, true);
    }

    @Bean
    public Queue filaResultadoReserva() {
        return new Queue(FILA_RESULTADO_RESERVA, true);
    }
}
