package br.com.coleta.sistemas.agendaecapacidade.Dto;

import java.util.UUID;

public record ResultadoReservaEventoDTO(
        UUID solicitacaoId,
        boolean reservada
) {
}
