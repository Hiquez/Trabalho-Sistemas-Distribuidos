package br.com.coleta.sistemas.solicitacoes.Dto;

import java.time.LocalDate;
import java.util.UUID;

public record SolicitacaoCriadaEventoDTO(
        UUID solicitacaoId,
        String bairro,
        LocalDate dataColeta) {
}
