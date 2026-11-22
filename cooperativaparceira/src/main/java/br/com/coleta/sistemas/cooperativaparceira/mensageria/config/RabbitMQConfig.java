
package br.com.coleta.sistemas.cooperativaparceira.mensageria.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "coleta.exchange";
    public static final String DLX = "cooperativa.dlx";

    public static final String FILA_SOLICITAR_ACEITE =
            "cooperativa.solicitar-aceite";

    public static final String FILA_CANCELAR_COMPROMISSO =
            "cooperativa.cancelar-compromisso";

    public static final String ROUTING_KEY_SOLICITAR_ACEITE =
            "cooperativa.solicitar-aceite";

    public static final String ROUTING_KEY_CANCELAR_COMPROMISSO =
            "cooperativa.cancelar-compromisso";

    public static final String DLQ_SOLICITAR_ACEITE =
            "cooperativa.solicitar-aceite.dlq";

    public static final String DLQ_CANCELAR_COMPROMISSO =
            "cooperativa.cancelar-compromisso.dlq";

    @Bean
    public DirectExchange coletaExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange cooperativaDlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue filaSolicitarAceite() {
        return QueueBuilder
                .durable(FILA_SOLICITAR_ACEITE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_SOLICITAR_ACEITE)
                .build();
    }

    @Bean
    public Queue filaCancelarCompromisso() {
        return QueueBuilder
                .durable(FILA_CANCELAR_COMPROMISSO)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_CANCELAR_COMPROMISSO)
                .build();
    }

    @Bean
    public Queue filaSolicitarAceiteDlq() {
        return QueueBuilder
                .durable(DLQ_SOLICITAR_ACEITE)
                .build();
    }

    @Bean
    public Queue filaCancelarCompromissoDlq() {
        return QueueBuilder
                .durable(DLQ_CANCELAR_COMPROMISSO)
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

    @Bean
    public Binding bindingSolicitarAceiteDlq(
            Queue filaSolicitarAceiteDlq,
            DirectExchange cooperativaDlx
    ) {
        return BindingBuilder
                .bind(filaSolicitarAceiteDlq)
                .to(cooperativaDlx)
                .with(DLQ_SOLICITAR_ACEITE);
    }

    @Bean
    public Binding bindingCancelarCompromissoDlq(
            Queue filaCancelarCompromissoDlq,
            DirectExchange cooperativaDlx
    ) {
        return BindingBuilder
                .bind(filaCancelarCompromissoDlq)
                .to(cooperativaDlx)
                .with(DLQ_CANCELAR_COMPROMISSO);
    }
}
