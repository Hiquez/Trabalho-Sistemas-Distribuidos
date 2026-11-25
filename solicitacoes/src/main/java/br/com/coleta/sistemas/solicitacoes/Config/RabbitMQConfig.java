package br.com.coleta.sistemas.solicitacoes.Config;
import org.springframework.amqp.core.*; import org.springframework.context.annotation.*; import java.util.*;
@Configuration public class RabbitMQConfig {
 public static final String EXCHANGE_SOLICITACOES="coleta.exchange";
 public static final String ROUTING_KEY_SOLICITACAO_CRIADA="solicitacao.criada";
 public static final String FILA_SOLICITACAO_CRIADA="agenda.criada";
 public static final String FILA_RESULTADO_RESERVA="solicitacoes.resultado";
 private String fila(String nome){return System.getenv().getOrDefault("MQ_QUEUE_"+nome.toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]","_"),nome);}
 @Bean public Declarables topologia(){var x=new DirectExchange("coleta.exchange",true,false); List<Declarable> all=new ArrayList<>(); all.add(x);
{var q=QueueBuilder.durable(fila("solicitacoes.resultado")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.resultado")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.resultado")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("reserva.resultado"));}
{var q=QueueBuilder.durable(fila("solicitacoes.aceite")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.aceite")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.aceite")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.aceitou"));}
{var q=QueueBuilder.durable(fila("solicitacoes.recusa")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.recusa")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.recusa")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.recusou"));}
{var q=QueueBuilder.durable(fila("solicitacoes.cancelado")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.cancelado")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.cancelado")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("cooperativa.compromisso-cancelado"));}
{var q=QueueBuilder.durable(fila("solicitacoes.executada")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.executada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.executada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.executada"));}
{var q=QueueBuilder.durable(fila("solicitacoes.triada")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.triada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.triada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.triada"));}
{var q=QueueBuilder.durable(fila("solicitacoes.destinada")).deadLetterExchange("").deadLetterRoutingKey(fila("solicitacoes.destinada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("solicitacoes.destinada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.destinada"));}
// Declare both destinations before the publisher can emit ColetaConfirmada.
{var q=QueueBuilder.durable(fila("agenda.confirmada")).deadLetterExchange("").deadLetterRoutingKey(fila("agenda.confirmada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("agenda.confirmada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.confirmada"));}
{var dlx=new DirectExchange("coletas.dlx",true,false); all.add(dlx); var n=fila("coletas.coleta-confirmada"); var q=QueueBuilder.durable(n).deadLetterExchange("coletas.dlx").deadLetterRoutingKey(n+".dlq").build(); var dlq=QueueBuilder.durable(n+".dlq").build(); all.add(q); all.add(dlq); all.add(BindingBuilder.bind(dlq).to(dlx).with(n+".dlq")); all.add(BindingBuilder.bind(q).to(x).with("coleta.confirmada"));}
return new Declarables(all);}}