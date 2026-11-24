
package br.com.coleta.sistemas.agendaecapacidade.Consumer;

import br.com.coleta.sistemas.agendaecapacidade.Config.RabbitMQConfig;
import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaRequestsDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.ResultadoReservaEventoDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.SolicitacaoCriadaEventoDTO;
import br.com.coleta.sistemas.agendaecapacidade.Repository.MensagemProcessadaRepository;
import br.com.coleta.sistemas.agendaecapacidade.Service.AgendaService;
import br.com.coleta.sistemas.agendaecapacidade.Service.OutboxEventoService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class SolicitacaoCriadaConsumer {

    private final ObjectMapper objectMapper;
    private final AgendaService agendaService;
    private final OutboxEventoService outboxEventoService;
    private final MensagemProcessadaRepository mensagemRepository;

    public SolicitacaoCriadaConsumer(ObjectMapper objectMapper, AgendaService agendaService, OutboxEventoService outboxEventoService, MensagemProcessadaRepository mensagemRepository) {

        this.objectMapper = objectMapper;
        this.agendaService = agendaService;
        this.outboxEventoService = outboxEventoService;
        this.mensagemRepository = mensagemRepository;
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.FILA_SOLICITACAO_CRIADA)
    public void receber(Message mensagem) {

        String corpo = new String(mensagem.getBody(), StandardCharsets.UTF_8);

        try {
            SolicitacaoCriadaEventoDTO evento = objectMapper.readValue(corpo, SolicitacaoCriadaEventoDTO.class);

            System.out.println("JSON RECEBIDO: " + corpo);
            System.out.println("SOLICITACAO ID: " + evento.solicitacaoId());

            String messageId = mensagem.getMessageProperties().getMessageId();

            if (messageId == null || messageId.isBlank()) {
                messageId = UUID.nameUUIDFromBytes(
                        corpo.getBytes(StandardCharsets.UTF_8)).toString();
            }

            boolean mensagemNova = mensagemRepository.registrarSeNovo(
                    messageId,
                    "SOLICITACAO_CRIADA"
            );

            if (!mensagemNova) {
                System.out.println("Mensagem duplicada ignorada: " + messageId);
                return;
            }

            AgendaRequestsDTO dto = new AgendaRequestsDTO(evento.solicitacaoId(), evento.bairro(), evento.dataColeta());

            boolean reservada;

            try {
                agendaService.reservar(dto);
                reservada = true;

                System.out.println("Reserva realizada: " + evento.solicitacaoId());

            }
            catch (IllegalStateException erro) {
                reservada = false;

                System.out.println("Reserva recusada: " + erro.getMessage());
            }

            ResultadoReservaEventoDTO resultado =
                    new ResultadoReservaEventoDTO(
                            evento.solicitacaoId(),
                            reservada
                    );

            String resposta = objectMapper.writeValueAsString(resultado);

            outboxEventoService.registrarEvento(evento.solicitacaoId(), "RESULTADO_RESERVA", resposta);

        }
        catch (JacksonException erro) {
            throw new IllegalArgumentException("Não foi possível converter o evento recebido.", erro);
        }
    }
}
