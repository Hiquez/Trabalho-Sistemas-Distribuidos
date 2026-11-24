package br.com.coleta.sistemas.agendaecapacidade.Dto;

import br.com.coleta.sistemas.agendaecapacidade.Entity.AgendaReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AgendaResponseDTO(
        UUID id,
        UUID solicitacaoId,
        String bairro,
        LocalDate dataColeta,
        AgendaReserva status,
        LocalDateTime criadoEm
) {
}
