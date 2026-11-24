package br.com.coleta.sistemas.agendaecapacidade.Consumer;

import br.com.coleta.sistemas.agendaecapacidade.Config.RabbitMQConfig;
import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaRequestsDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.ResultadoReservaEventoDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.SolicitacaoCriadaEventoDTO;
import br.com.coleta.sistemas.agendaecapacidade.Service.AgendaService;
import br.com.coleta.sistemas.agendaecapacidade.Service.OutboxEventoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class SolicitacaoCriadaConsumer {

    private final ObjectMapper objectMapper;
    private final AgendaService agendaService;
    private final OutboxEventoService outboxEventoService;

    public SolicitacaoCriadaConsumer(ObjectMapper objectMapper, AgendaService agendaService,  OutboxEventoService outboxEventoService) {
        this.objectMapper = objectMapper;
        this.agendaService = agendaService;
        this.outboxEventoService = outboxEventoService;
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.FILA_SOLICITACAO_CRIADA)
    public void receber(String mensagem) {

        try {
            SolicitacaoCriadaEventoDTO evento = objectMapper.readValue(mensagem, SolicitacaoCriadaEventoDTO.class);

            AgendaRequestsDTO dto = new AgendaRequestsDTO(
                    evento.solicitacaoId(), evento.bairro(), evento.dataColeta());

            boolean reservada;

            try {
                agendaService.reservar(dto);
                reservada = true;

                System.out.println("Reserva realizada: " + evento.solicitacaoId());
            } catch (IllegalStateException erro) {
                reservada = false;

                System.out.println("Reserva recusada: " + erro.getMessage());
            }

            ResultadoReservaEventoDTO resultado = new ResultadoReservaEventoDTO(evento.solicitacaoId(), reservada);
            String resposta = objectMapper.writeValueAsString(resultado);

            outboxEventoService.registrarEvento(evento.solicitacaoId(), "RESULTADO_RESERVA", resposta);
        }
        catch (JacksonException erro) {
            throw new IllegalArgumentException("Não foi possível converter o evento recebido.", erro);
        }
    }
}
