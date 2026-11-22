package br.com.coleta.sistemas.cooperativaparceira.mensageria.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "coleta.exchange";

    public static final String FILA_SOLICITAR_ACEITE =
            "cooperativa.solicitar-aceite";

    public static final String FILA_CANCELAR_COMPROMISSO =
            "cooperativa.cancelar-compromisso";

    public static final String ROUTING_KEY_SOLICITAR_ACEITE =
            "cooperativa.solicitar-aceite";

    public static final String ROUTING_KEY_CANCELAR_COMPROMISSO =
            "cooperativa.cancelar-compromisso";

    @Bean
    public DirectExchange coletaExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaSolicitarAceite() {
        return QueueBuilder
                .durable(FILA_SOLICITAR_ACEITE)
                .build();
    }

    @Bean
    public Queue filaCancelarCompromisso() {
        return QueueBuilder
                .durable(FILA_CANCELAR_COMPROMISSO)
                .build();
    }

    @Bean
    public Binding bindingSolicitarAceite(
            Queue filaSolicitarAceite,
            DirectExchange coletaExchange
    ) {
        return BindingBuilder
                .bind(filaSolicitarAceite)
                .to(coletaExchange)
                .with(ROUTING_KEY_SOLICITAR_ACEITE);
    }

    @Bean
    public Binding bindingCancelarCompromisso(
            Queue filaCancelarCompromisso,
            DirectExchange coletaExchange
    ) {
        return BindingBuilder
                .bind(filaCancelarCompromisso)
                .to(coletaExchange)
                .with(ROUTING_KEY_CANCELAR_COMPROMISSO);
    }
}