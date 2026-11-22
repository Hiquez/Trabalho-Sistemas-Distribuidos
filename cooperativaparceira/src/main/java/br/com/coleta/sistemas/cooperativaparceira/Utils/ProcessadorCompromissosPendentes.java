package br.com.coleta.sistemas.cooperativaparceira.Utils;

import br.com.coleta.sistemas.cooperativaparceira.Entity.Compromisso;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusCompromisso;
import br.com.coleta.sistemas.cooperativaparceira.repository.CompromissoRepository;
import br.com.coleta.sistemas.cooperativaparceira.service.CompromissoService;
import br.com.coleta.sistemas.cooperativaparceira.service.SimulacaoCooperativaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessadorCompromissosPendentes {

    private final CompromissoRepository compromissoRepository;
    private final CompromissoService compromissoService;
    private final SimulacaoCooperativaService simulacaoCooperativaService;

    @Scheduled(fixedDelay = 5000)
    public void processarPendentes() {

        List<Compromisso> pendentes =
                compromissoRepository.findByStatusOrderByCriadoEmAsc(
                        StatusCompromisso.EM_ANALISE,
                        PageRequest.of(0, 50)
                );

        for (Compromisso compromisso : pendentes) {

            try {
                simulacaoCooperativaService.simularLatencia();
                simulacaoCooperativaService.simularFalha();

                compromissoService.processarSolicitacaoAceite(
                        compromisso.getIdCompromisso(),
                        compromisso.getSolicitacaoId(),
                        compromisso.getCorrelationId()
                );

            } catch (Exception e) {
                log.error(
                        "Erro ao processar compromisso {}",
                        compromisso.getIdCompromisso(),
                        e
                );
            }
        }
    }
}