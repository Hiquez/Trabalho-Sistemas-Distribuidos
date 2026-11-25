
package br.com.coleta.sistemas.solicitacoes.Service;

import br.com.coleta.sistemas.solicitacoes.Dto.SolicitacaoCriadaEventoDTO;
import br.com.coleta.sistemas.solicitacoes.Dto.SolicitacaoRequestDTO;
import br.com.coleta.sistemas.solicitacoes.Dto.SolicitacaoResponseDTO;
import br.com.coleta.sistemas.solicitacoes.Entity.OutboxEvento;
import br.com.coleta.sistemas.solicitacoes.Entity.Solicitacao;
import br.com.coleta.sistemas.solicitacoes.Entity.StatusSolicitacao;
import br.com.coleta.sistemas.solicitacoes.Repository.OutboxEventoRepository;
import br.com.coleta.sistemas.solicitacoes.Repository.SolicitacaoRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final SolicitacaoRespository solicitacaoRespository;
    private final OutboxEventoRepository outboxEventoRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO dto) {

        Solicitacao solicitacao = new Solicitacao(dto.bairro(), dto.dataColeta());
        Solicitacao salva = solicitacaoRespository.save(solicitacao);

        SolicitacaoCriadaEventoDTO evento = new SolicitacaoCriadaEventoDTO(
                salva.getId(),
                salva.getBairro(),
                salva.getDataColeta()
        );

        try {
            String payload = objectMapper.writeValueAsString(br.com.coleta.sistemas.solicitacoes.Dto.Envelope.criar(salva.getId(), "SolicitacaoCriada", java.util.Map.of("solicitacaoId",salva.getId(),"bairro",salva.getBairro(),"dataColeta",salva.getDataColeta().toString())));
            OutboxEvento outbox = new OutboxEvento(
                    salva.getId(), "solicitacao.criada", payload
            );

            outboxEventoRepository.save(outbox);

        } catch (JacksonException erro) {
            throw new IllegalStateException(
                    "Não foi possível criar o evento da solicitação.", erro
            );
        }

        return converterParaResponse(salva);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponseDTO buscarPorId(UUID id) {

        Solicitacao solicitacao = solicitacaoRespository.findById(id).orElseThrow(
                () -> new RuntimeException("Solicitação não encontrada")
        );

        return converterParaResponse(solicitacao);
    }

    private SolicitacaoResponseDTO converterParaResponse(Solicitacao solicitacao) {

        String mensagem = switch (solicitacao.getStatus()) {
            case PENDENTE -> "Solicitação recebida e aguardando reserva de capacidade.";
            case AGUARDANDO_RESERVA -> "Solicitação aguardando confirmação de disponibilidade.";
            case AGUARDANDO_COOPERATIVA -> "Reserva realizada. Aguardando confirmação da cooperativa.";
            case CONFIRMADA -> "Solicitação confirmada com sucesso.";
            case CANCELADA -> "Solicitação cancelada: não há vagas disponíveis para o bairro "
                    + solicitacao.getBairro() + " na data "
                    + solicitacao.getDataColeta() + ".";
        };

        return new SolicitacaoResponseDTO(
                solicitacao.getId(),
                solicitacao.getBairro(),
                solicitacao.getDataColeta(),
                solicitacao.getStatus(),
                solicitacao.getCriadoEm(),
                mensagem
        );
    }

    @Transactional
    public boolean atualizarStatus(UUID id, StatusSolicitacao novoStatus) {

        Solicitacao solicitacao = solicitacaoRespository
                .buscarPorIdComBloqueio(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        boolean atualizado = solicitacao.atualizarStatus(novoStatus);

        if (atualizado) {
            solicitacaoRespository.save(solicitacao);
        }

        return atualizado;
    }
}
