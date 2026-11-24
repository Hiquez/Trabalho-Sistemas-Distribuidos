package br.com.coleta.sistemas.solicitacoes.Dto;

import java.util.UUID;

public record ResultadoReservaEventoDTO(
        UUID solicitacaoId,
        Boolean reservada
) {
}
