package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Entity.MensagemProcessada;
import br.com.coleta.sistemas.cooperativaparceira.repository.MensagemProcessadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotenciaService {

    private final MensagemProcessadaRepository mensagemProcessadaRepository;

    public Boolean validarMensagemProcessada(UUID messageId) {
        return mensagemProcessadaRepository.existsByMessageId(messageId);
    }

    public void registrarMensagem(UUID messageId) {
        registrarMensagem(messageId, null, null, null);
    }

    public void registrarMensagem(
            UUID messageId,
            UUID correlationId,
            UUID solicitacaoId,
            String tipoMensagem
    ) {
        MensagemProcessada mensagem = new MensagemProcessada();

        mensagem.setMessageId(messageId);
        mensagem.setCorrelationId(correlationId);
        mensagem.setSolicitacaoId(solicitacaoId);
        mensagem.setTipoMensagem(tipoMensagem);
        mensagem.setProcessadaEm(Instant.now());

        mensagemProcessadaRepository.saveAndFlush(mensagem);
    }
}
