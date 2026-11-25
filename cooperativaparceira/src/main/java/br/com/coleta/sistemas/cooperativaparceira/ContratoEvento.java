package br.com.coleta.sistemas.cooperativaparceira;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;
/** Validacao dos campos e formatos dos contratos v1, antes da conversao tipada. */
public final class ContratoEvento {
 private ContratoEvento() {}
 private static void falha(String campo) { throw new IllegalArgumentException("Contrato invalido: " + campo); }
 private static JsonNode campo(JsonNode n,String k) { if(n==null || !n.isObject() || !n.has(k)) falha(k); return n.get(k); }
 private static void texto(JsonNode n) { if(!n.isTextual()) falha("string"); }
 private static void uuid(JsonNode n) { texto(n); String s=n.asText(); if(!UUID.fromString(s).toString().equalsIgnoreCase(s)) falha("uuid"); }
 private static void instante(JsonNode n) { texto(n); OffsetDateTime.parse(n.asText()); }
 private static void data(JsonNode n) { texto(n); LocalDate.parse(n.asText()); }
 private static void numero(JsonNode n) { if(!n.isNumber()) falha("number"); }
 private static void booleano(JsonNode n) { if(!n.isBoolean()) falha("boolean"); }
 private static void inteiro(JsonNode n) { if(!n.isIntegralNumber()) falha("integer"); }
 public static void validar(ObjectMapper mapper,String body,String tipo) {
  JsonNode e=mapper.readTree(body); uuid(campo(e,"messageId")); uuid(campo(e,"correlationId")); instante(campo(e,"occurredAt"));
  JsonNode v=campo(e,"version"); inteiro(v); if(v.asInt()!=1 || !tipo.equals(campo(e,"type").asText())) falha("type/version");
  JsonNode p=campo(e,"payload"); if(!p.isObject()) falha("payload");
  switch(tipo) {
  case "CancelarCompromisso" -> { campo(p, "solicitacaoId"); campo(p, "motivoCancelamento"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("motivoCancelamento")) texto(p.get("motivoCancelamento")); }
  case "ColetaConfirmada" -> { campo(p, "solicitacaoId"); campo(p, "reservaId"); campo(p, "compromissoId"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); }
  case "ColetaDestinada" -> { campo(p, "solicitacaoId"); campo(p, "coletaId"); campo(p, "status"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); if (p.has("coletaId")) uuid(p.get("coletaId")); if (p.has("status") && !"DESTINADA".equals(p.get("status").asText())) falha("status"); }
  case "ColetaExecutada" -> { campo(p, "solicitacaoId"); campo(p, "coletaId"); campo(p, "status"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); if (p.has("coletaId")) uuid(p.get("coletaId")); if (p.has("status") && !"EXECUTADA".equals(p.get("status").asText())) falha("status"); }
  case "ColetaTriada" -> { campo(p, "solicitacaoId"); campo(p, "coletaId"); campo(p, "status"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); if (p.has("coletaId")) uuid(p.get("coletaId")); if (p.has("status") && !"TRIADA".equals(p.get("status").asText())) falha("status"); }
  case "CompromissoCancelado" -> { campo(p, "solicitacaoId"); campo(p, "compromissoId"); campo(p, "motivoCancelamento"); campo(p, "dataCancelamento"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("compromissoId") && !p.get("compromissoId").isNull()) uuid(p.get("compromissoId")); if (p.has("motivoCancelamento")) texto(p.get("motivoCancelamento")); if (p.has("dataCancelamento")) instante(p.get("dataCancelamento")); }
  case "CooperativaAceitou" -> { campo(p, "solicitacaoId"); campo(p, "compromissoId"); campo(p, "reservaId"); campo(p, "dataAceite"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("dataAceite")) instante(p.get("dataAceite")); }
  case "CooperativaRecusou" -> { campo(p, "solicitacaoId"); campo(p, "compromissoId"); campo(p, "motivoRecusa"); campo(p, "dataRecusa"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("compromissoId")) uuid(p.get("compromissoId")); if (p.has("motivoRecusa")) texto(p.get("motivoRecusa")); if (p.has("dataRecusa")) instante(p.get("dataRecusa")); }
  case "LiberarReserva" -> { campo(p, "solicitacaoId"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); }
  case "ResultadoReserva" -> { campo(p, "solicitacaoId"); campo(p, "reservada"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservada")) booleano(p.get("reservada")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.get("reservada").asBoolean()) uuid(campo(p, "reservaId")); }
  case "SolicitacaoCriada" -> { campo(p, "solicitacaoId"); campo(p, "bairro"); campo(p, "dataColeta"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("bairro")) texto(p.get("bairro")); if (p.has("dataColeta")) data(p.get("dataColeta")); }
  case "SolicitarAceiteCooperativa" -> { campo(p, "solicitacaoId"); campo(p, "reservaId"); campo(p, "categoriaResiduo"); campo(p, "quantidadeEstimada"); campo(p, "dataAgendada"); if (p.has("solicitacaoId")) uuid(p.get("solicitacaoId")); if (p.has("reservaId")) uuid(p.get("reservaId")); if (p.has("categoriaResiduo")) texto(p.get("categoriaResiduo")); if (p.has("quantidadeEstimada")) numero(p.get("quantidadeEstimada")); if (p.has("dataAgendada")) instante(p.get("dataAgendada")); }
  default -> falha("type");
  }
 }
}
