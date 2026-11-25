package br.com.coleta.sistemas.agendaecapacidade.Config;
import org.springframework.amqp.core.*; import org.springframework.context.annotation.*; import java.util.*;
@Configuration public class RabbitMQConfig {
 public static final String EXCHANGE_SOLICITACOES="coleta.exchange";
 public static final String ROUTING_KEY_SOLICITACAO_CRIADA="solicitacao.criada";
 public static final String FILA_SOLICITACAO_CRIADA="agenda.criada";
 public static final String FILA_RESULTADO_RESERVA="solicitacoes.resultado";
 private String fila(String nome){return System.getenv().getOrDefault("MQ_QUEUE_"+nome.toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]","_"),nome);}
 @Bean public Declarables topologia(){var x=new DirectExchange("coleta.exchange",true,false); List<Declarable> all=new ArrayList<>(); all.add(x);
{var q=QueueBuilder.durable(fila("agenda.criada")).deadLetterExchange("").deadLetterRoutingKey(fila("agenda.criada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("agenda.criada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("solicitacao.criada"));}
{var q=QueueBuilder.durable(fila("agenda.liberar")).deadLetterExchange("").deadLetterRoutingKey(fila("agenda.liberar")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("agenda.liberar")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("agenda.liberar-reserva"));}
{var q=QueueBuilder.durable(fila("agenda.confirmada")).deadLetterExchange("").deadLetterRoutingKey(fila("agenda.confirmada")+".dlq").build(); all.add(q); all.add(QueueBuilder.durable(fila("agenda.confirmada")+".dlq").build()); all.add(BindingBuilder.bind(q).to(x).with("coleta.confirmada"));}
return new Declarables(all);}}