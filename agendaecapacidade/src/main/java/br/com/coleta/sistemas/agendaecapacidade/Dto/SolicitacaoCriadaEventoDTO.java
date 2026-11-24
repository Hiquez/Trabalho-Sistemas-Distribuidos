package br.com.coleta.sistemas.agendaecapacidade.Dto;

import java.time.LocalDate;
import java.util.UUID;

public record SolicitacaoCriadaEventoDTO(
        UUID solicitacaoId,
        String bairro,
        LocalDate dataColeta
) {
}
