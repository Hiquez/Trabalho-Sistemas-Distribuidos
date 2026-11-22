package br.com.coleta.sistemas.cooperativaparceira.mensageria.evento;

import java.util.UUID;

public record CancelarCompromissoEvent(UUID solicitacaoId, String motivoCancelamento) {}
