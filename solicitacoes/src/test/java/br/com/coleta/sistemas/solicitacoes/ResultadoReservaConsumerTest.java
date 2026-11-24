package br.com.coleta.sistemas.solicitacoes;

import br.com.coleta.sistemas.solicitacoes.Consumer.ResultadoReservaConsumer;
import br.com.coleta.sistemas.solicitacoes.Entity.StatusSolicitacao;
import br.com.coleta.sistemas.solicitacoes.Repository.MensagemProcessadaRepository;
import br.com.coleta.sistemas.solicitacoes.Service.SolicitacaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultadoReservaConsumerTest {

    private ObjectMapper objectMapper;
    private SolicitacaoService solicitacaoService;
    private MensagemProcessadaRepository mensagemRepository;
    private ResultadoReservaConsumer consumer;

    private final UUID solicitacaoId =
            UUID.fromString("87b851de-1abb-48aa-963b-acab72e94c66");

    @BeforeEach
    void configurar() {
        objectMapper = new ObjectMapper();
        solicitacaoService = mock(SolicitacaoService.class);
        mensagemRepository = mock(MensagemProcessadaRepository.class);

        consumer = new ResultadoReservaConsumer(objectMapper, solicitacaoService, mensagemRepository);
    }

    private Message criarMensagem(String corpo) {
        MessageProperties propriedades = new MessageProperties();
        propriedades.setMessageId(UUID.randomUUID().toString());

        return new Message(corpo.getBytes(StandardCharsets.UTF_8), propriedades);
    }

    @Test
    void deveAtualizarStatusQuandoReservaForAprovada() {
        String json = """
                {
                    "solicitacaoId": "%s",
                    "reservada": true
                }
                """.formatted(solicitacaoId);

        Message mensagem = criarMensagem(json);

        when(mensagemRepository.registrarSeNovo(anyString(), eq("RESULTADO_RESERVA"))).thenReturn(true);
        when(solicitacaoService.atualizarStatus(solicitacaoId, StatusSolicitacao.AGUARDANDO_COOPERATIVA)).thenReturn(true);
        assertDoesNotThrow(() -> consumer.receber(mensagem));
        verify(solicitacaoService).atualizarStatus(solicitacaoId, StatusSolicitacao.AGUARDANDO_COOPERATIVA);
    }

    @Test
    void deveCancelarSolicitacaoQuandoReservaForRecusada() {
        String json = """
                {
                    "solicitacaoId": "%s",
                    "reservada": false
                }
                """.formatted(solicitacaoId);

        Message mensagem = criarMensagem(json);

        when(mensagemRepository.registrarSeNovo(anyString(), eq("RESULTADO_RESERVA"))).thenReturn(true);
        when(solicitacaoService.atualizarStatus(solicitacaoId, StatusSolicitacao.CANCELADA)).thenReturn(true);
        assertDoesNotThrow(() -> consumer.receber(mensagem));
        verify(solicitacaoService).atualizarStatus(solicitacaoId, StatusSolicitacao.CANCELADA);
    }

    @Test
    void naoDeveProcessarMensagemDuplicada() {
        String json = """
                {
                    "solicitacaoId": "%s",
                    "reservada": true
                }
                """.formatted(solicitacaoId);

        Message mensagem = criarMensagem(json);

        when(mensagemRepository.registrarSeNovo(anyString(), eq("RESULTADO_RESERVA"))).thenReturn(false);
        assertDoesNotThrow(() -> consumer.receber(mensagem));
        verify(solicitacaoService, never()).atualizarStatus(any(), any());
    }

    @Test
    void deveRejeitarJsonInvalido() {
        Message mensagem = criarMensagem("{ json inválido ");

        assertThrows(IllegalArgumentException.class, () -> consumer.receber(mensagem));
        verifyNoInteractions(mensagemRepository);
        verifyNoInteractions(solicitacaoService);
    }

    @Test
    void deveRejeitarSolicitacaoIdNulo() {
        String json = """
                {
                    "solicitacaoId": null,
                    "reservada": true
                }
                """;

        Message mensagem = criarMensagem(json);

        assertThrows(IllegalArgumentException.class, () -> consumer.receber(mensagem));
        verifyNoInteractions(mensagemRepository);
        verifyNoInteractions(solicitacaoService);
    }

    @Test
    void deveRejeitarCampoReservadaAusente() {
        String json = """
                {
                    "solicitacaoId": "%s"
                }
                """.formatted(solicitacaoId);

        Message mensagem = criarMensagem(json);

        assertThrows(IllegalArgumentException.class, () -> consumer.receber(mensagem));
        verifyNoInteractions(mensagemRepository);
        verifyNoInteractions(solicitacaoService);
    }
}
