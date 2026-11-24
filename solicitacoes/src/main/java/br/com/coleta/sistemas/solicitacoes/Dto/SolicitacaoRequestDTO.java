package br.com.coleta.sistemas.solicitacoes.Dto;

import java.time.LocalDate;

public record SolicitacaoRequestDTO(
        String bairro,
        LocalDate dataColeta) {
}