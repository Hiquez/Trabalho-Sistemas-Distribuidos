package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Entity.MensagemProcessada;
import br.com.coleta.sistemas.cooperativaparceira.repository.MensagemProcessadaRepository;
import java.time.Instant;
import java.util.UUID;

public class IdempotenciaService {

    private MensagemProcessadaRepository mensagemProcessadaRepository;

    public IdempotenciaService(MensagemProcessadaRepository mensagemProcessadaRepository) {
        this.mensagemProcessadaRepository = mensagemProcessadaRepository;
    }

    public Boolean validarMensagemProcessada(UUID idMensagem){
        return mensagemProcessadaRepository.existsByMessageId(idMensagem);
    }

    public void registrarMensagem(UUID messageId) {

        MensagemProcessada mensagem = new MensagemProcessada();

        mensagem.setMessageId(messageId);
        mensagem.setProcessadaEm(Instant.now());

        mensagemProcessadaRepository.saveAndFlush(mensagem);
    }
}
