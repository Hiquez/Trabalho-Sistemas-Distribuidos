package br.com.coleta.sistemas.solicitacoes.Dto;

import br.com.coleta.sistemas.solicitacoes.Entity.StatusSolicitacao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoResponseDTO(
        UUID id,
        String bairro,
        LocalDate dataColeta,
        StatusSolicitacao status,
        LocalDateTime criadoEm,
        String mensagem
) {
}