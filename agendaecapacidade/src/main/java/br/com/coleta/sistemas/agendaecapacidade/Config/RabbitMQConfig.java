package br.com.coleta.sistemas.agendaecapacidade.Config;
import org.springframework.amqp.core.*; import org.springframework.context.annotation.*; import java.util.*;
@Configuration public class RabbitMQConfig {
 public static final String EXCHANGE_SOLICITACOES="coleta.exchange";
 public static final String ROUTING_KEY_SOLICITACAO_CRIADA="solicitacao.criada";
 public static final String FILA_SOLICITACAO_CRIADA="agenda.criada";
 public static final String FILA_RESULTADO_RESERVA="solicitacoes.resultado";
 @Bean public Declarables topologia(){var x=new DirectExchange("coleta.exchange",true,false); List<Declarable> all=new ArrayList<>(); all.add(x);
{var q=QueueBuilder.durable("agenda.criada").deadLetterExchange("").deadLetterRoutingKey("agenda.criada.dlq").build(); all.add(q); all.add(QueueBuilder.durable("agenda.criada.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("solicitacao.criada"));}
{var q=QueueBuilder.durable("agenda.liberar").deadLetterExchange("").deadLetterRoutingKey("agenda.liberar.dlq").build(); all.add(q); all.add(QueueBuilder.durable("agenda.liberar.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("agenda.liberar-reserva"));}
{var q=QueueBuilder.durable("agenda.confirmada").deadLetterExchange("").deadLetterRoutingKey("agenda.confirmada.dlq").build(); all.add(q); all.add(QueueBuilder.durable("agenda.confirmada.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.confirmada"));}
return new Declarables(all);}}