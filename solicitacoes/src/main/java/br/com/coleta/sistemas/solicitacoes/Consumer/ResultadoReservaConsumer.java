package br.com.coleta.sistemas.solicitacoes.Consumer;

import br.com.coleta.sistemas.solicitacoes.Config.RabbitMQConfig;
import br.com.coleta.sistemas.solicitacoes.Dto.ResultadoReservaEventoDTO;
import br.com.coleta.sistemas.solicitacoes.Entity.StatusSolicitacao;
import br.com.coleta.sistemas.solicitacoes.Repository.MensagemProcessadaRepository;
import br.com.coleta.sistemas.solicitacoes.Service.SolicitacaoService;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class ResultadoReservaConsumer {

    private final ObjectMapper objectMapper;
    private final SolicitacaoService solicitacaoService;
    private final MensagemProcessadaRepository mensagemRepository;

    public ResultadoReservaConsumer(ObjectMapper objectMapper, SolicitacaoService solicitacaoService, MensagemProcessadaRepository mensagemRepository) {
        this.objectMapper = objectMapper;
        this.solicitacaoService = solicitacaoService;
        this.mensagemRepository = mensagemRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_RESULTADO_RESERVA)
    @Transactional
    public void receber(Message mensagem) {

        String corpo = new String(
                mensagem.getBody(),
                StandardCharsets.UTF_8
        );

        try {
            ResultadoReservaEventoDTO evento = objectMapper.readValue(corpo, ResultadoReservaEventoDTO.class);

            String messageId = mensagem.getMessageProperties().getMessageId();

            if (messageId == null || messageId.isBlank()) {messageId = UUID.nameUUIDFromBytes(corpo.getBytes(StandardCharsets.UTF_8)).toString();
            }

            boolean mensagemNova = mensagemRepository.registrarSeNovo(messageId, "RESULTADO_RESERVA");

            if (!mensagemNova) {
                return;
            }

            StatusSolicitacao novoStatus = evento.reservada() ? StatusSolicitacao.AGUARDANDO_COOPERATIVA : StatusSolicitacao.CANCELADA;

            boolean atualizado = solicitacaoService.atualizarStatus(evento.solicitacaoId(), novoStatus);

            if (atualizado) {
                System.out.println("Status atualizado para: " + novoStatus);
            }
            else {
                System.out.println("Mensagem ignorada: solicitação já avançou ou foi cancelada.");
            }

        }
        catch (JacksonException erro) {
            throw new IllegalArgumentException("Não foi possível converter o resultado da reserva.", erro);
        }
    }
}
