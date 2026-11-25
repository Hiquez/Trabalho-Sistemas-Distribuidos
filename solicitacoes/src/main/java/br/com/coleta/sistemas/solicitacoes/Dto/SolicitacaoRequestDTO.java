package br.com.coleta.sistemas.solicitacoes.Dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoRequestDTO(
        @NotBlank @Size(max = 255) String bairro,
        @NotNull LocalDate dataColeta) {
}