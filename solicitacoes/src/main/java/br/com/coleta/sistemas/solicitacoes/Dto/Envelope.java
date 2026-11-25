package br.com.coleta.sistemas.solicitacoes.Dto;
import java.util.UUID; import java.time.Instant; import java.util.Map;
public record Envelope(UUID messageId, UUID correlationId, String type, Integer version, Instant occurredAt, Map<String,Object> payload) {
 public static Envelope criar(UUID corr,String tipo,Map<String,Object> p){return new Envelope(UUID.randomUUID(),corr,tipo,1,Instant.now(),p);}
 public void validar(String tipo){if(messageId==null||correlationId==null||occurredAt==null||!Integer.valueOf(1).equals(version)||!tipo.equals(type)||payload==null)throw new IllegalArgumentException("Envelope invalido");}
 public UUID id(String k){return UUID.fromString((String)payload.get(k));}
}