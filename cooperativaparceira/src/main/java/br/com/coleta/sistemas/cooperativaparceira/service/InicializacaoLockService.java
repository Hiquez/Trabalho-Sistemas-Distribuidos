package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.repository.SolicitacaoLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InicializacaoLockService {

    private final SolicitacaoLockRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inicializar(UUID solicitacaoId) {
        repository.inserir(solicitacaoId);
    }
}