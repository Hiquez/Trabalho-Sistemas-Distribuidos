package br.com.coleta.sistemas.solicitacoes.Config;
import org.springframework.amqp.core.*; import org.springframework.context.annotation.*; import java.util.*;
@Configuration public class RabbitMQConfig {
 public static final String EXCHANGE_SOLICITACOES="coleta.exchange";
 public static final String ROUTING_KEY_SOLICITACAO_CRIADA="solicitacao.criada";
 public static final String FILA_SOLICITACAO_CRIADA="agenda.criada";
 public static final String FILA_RESULTADO_RESERVA="solicitacoes.resultado";
 @Bean public Declarables topologia(){var x=new DirectExchange("coleta.exchange",true,false); List<Declarable> all=new ArrayList<>(); all.add(x);
{var q=QueueBuilder.durable("solicitacoes.resultado").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.resultado.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.resultado.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("reserva.resultado"));}
{var q=QueueBuilder.durable("solicitacoes.aceite").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.aceite.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.aceite.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.aceitou"));}
{var q=QueueBuilder.durable("solicitacoes.recusa").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.recusa.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.recusa.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.recusou"));}
{var q=QueueBuilder.durable("solicitacoes.cancelado").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.cancelado.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.cancelado.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.compromisso-cancelado"));}
{var q=QueueBuilder.durable("solicitacoes.executada").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.executada.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.executada.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.executada"));}
{var q=QueueBuilder.durable("solicitacoes.triada").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.triada.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.triada.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.triada"));}
{var q=QueueBuilder.durable("solicitacoes.destinada").deadLetterExchange("").deadLetterRoutingKey("solicitacoes.destinada.dlq").build(); all.add(q); all.add(QueueBuilder.durable("solicitacoes.destinada.dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.destinada"));}
return new Declarables(all);}}