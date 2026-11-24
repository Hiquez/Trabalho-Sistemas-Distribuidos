package br.com.coleta.sistemas.agendaecapacidade.Dto;

import java.time.LocalDate;

public record CapacidadeAgendaRequestDTO(
        String bairro,
        LocalDate dataColeta,
        Integer capacidadeTotal
) {
}
