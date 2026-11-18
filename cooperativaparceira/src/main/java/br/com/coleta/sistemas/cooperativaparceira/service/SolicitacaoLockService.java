package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.repository.SolicitacaoLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitacaoLockService {

    private final InicializacaoLockService inicializacao;
    private final SolicitacaoLockRepository repository;

    public void garantirExistencia(UUID solicitacaoId) {
        try {
            inicializacao.inicializar(solicitacaoId);
        } catch (DataIntegrityViolationException ex) {
            if (!repository.existsById(solicitacaoId)) {
                throw ex;
            }
        }
    }

    public void bloquear(UUID solicitacaoId) {
        repository.bloquear(solicitacaoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Lock não encontrado: " + solicitacaoId
                ));
    }
}