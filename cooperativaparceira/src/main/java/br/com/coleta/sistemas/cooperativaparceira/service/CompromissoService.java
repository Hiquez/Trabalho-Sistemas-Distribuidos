
package br.com.coleta.sistemas.cooperativaparceira.service;

import br.com.coleta.sistemas.cooperativaparceira.Entity.CancelamentoAntecipado;
import br.com.coleta.sistemas.cooperativaparceira.Entity.Compromisso;
import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusCompromisso;
import br.com.coleta.sistemas.cooperativaparceira.mensageria.evento.*;
import br.com.coleta.sistemas.cooperativaparceira.repository.CancelamentoAntecipadoRepository;
import br.com.coleta.sistemas.cooperativaparceira.repository.CompromissoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final ValidarEventoService validarEventoService;
    private final IdempotenciaService idempotenciaService;
    private final SimulacaoCooperativaService simulacaoCooperativaService;
    private final OutboxService outboxService;
    private final CancelamentoAntecipadoRepository cancelamentoAntecipadoRepository;
    private final SolicitacaoLockService solicitacaoLockService;

    @Transactional
    public UUID criarCompromisso(
            EventEnvelope<SolicitarAceiteCooperativaEvent> envelope
    ) {
        validarEventoService.validarSolicitacaoAceite(envelope);

        SolicitarAceiteCooperativaEvent evento = envelope.payload();
        UUID solicitacaoId = evento.solicitacaoId();

        // A linha de lock deve existir antes desta transação.
        solicitacaoLockService.bloquear(solicitacaoId);

        if (idempotenciaService.validarMensagemProcessada(
                envelope.messageId())) {

            return compromissoRepository
                    .findBySolicitacaoId(solicitacaoId)
                    .map(Compromisso::getIdCompromisso)
                    .orElse(null);
        }

        idempotenciaService.registrarMensagem(envelope.messageId());

        // Cancelamento recebido antes da criação.
        if (cancelamentoAntecipadoRepository
                .existsBySolicitacaoId(solicitacaoId)) {
            return null;
        }

        Optional<Compromisso> existente =
                compromissoRepository.findBySolicitacaoId(solicitacaoId);

        if (existente.isPresent()) {
            return existente.get().getIdCompromisso();
        }

        Instant agora = Instant.now();

        Compromisso compromisso = new Compromisso();
        compromisso.setSolicitacaoId(solicitacaoId);
        compromisso.setReservaId(evento.reservaId());
        compromisso.setStatus(StatusCompromisso.EM_ANALISE);
        compromisso.setCriadoEm(agora);
        compromisso.setAtualizadoEm(agora);

        return compromissoRepository
                .save(compromisso)
                .getIdCompromisso();
    }

    @Transactional
    public void processarSolicitacaoAceite(
            UUID compromissoId,
            UUID solicitacaoId,
            UUID correlationId
    ) {
        // Mesmo lock utilizado pela criação e cancelamento.
        solicitacaoLockService.bloquear(solicitacaoId);

        Compromisso compromisso = compromissoRepository
                .findById(compromissoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Compromisso não encontrado: " + compromissoId
                        )
                );

        if (!compromisso.getSolicitacaoId().equals(solicitacaoId)) {
            throw new IllegalArgumentException(
                    "Compromisso não pertence à solicitação: "
                            + solicitacaoId
            );
        }

        if (compromisso.getStatus() != StatusCompromisso.EM_ANALISE) {
            return;
        }

        if (compromisso.getCanceladoEm() != null) {
            return;
        }

        boolean aceitou = simulacaoCooperativaService.deveAceitar();

        if (aceitou) {
            aceitarCompromisso(compromisso, correlationId);
        } else {
            recusarCompromisso(compromisso, correlationId);
        }
    }

    private void aceitarCompromisso(
            Compromisso compromisso,
            UUID correlationId
    ) {
        Instant agora = Instant.now();

        compromisso.setStatus(StatusCompromisso.ACEITO);
        compromisso.setAtualizadoEm(agora);

        compromissoRepository.save(compromisso);

        CooperativaAceitouEvent evento = new CooperativaAceitouEvent(
                compromisso.getSolicitacaoId(),
                compromisso.getIdCompromisso(),
                compromisso.getReservaId(),
                agora
        );

        outboxService.registrar(
                correlationId,
                "CooperativaAceitou",
                evento
        );
    }

    private void recusarCompromisso(
            Compromisso compromisso,
            UUID correlationId
    ) {
        Instant agora = Instant.now();

        compromisso.setStatus(StatusCompromisso.RECUSADO);
        compromisso.setAtualizadoEm(agora);

        compromissoRepository.save(compromisso);

        CooperativaRecusouEvent evento = new CooperativaRecusouEvent(
                compromisso.getSolicitacaoId(),
                compromisso.getIdCompromisso(),
                "RECUSA_SIMULADA",
                agora
        );

        outboxService.registrar(
                correlationId,
                "CooperativaRecusou",
                evento
        );
    }

    @Transactional
    public void cancelarCompromisso(
            EventEnvelope<CancelarCompromissoEvent> envelope
    ) {
        if (envelope == null
                || envelope.messageId() == null
                || envelope.correlationId() == null
                || envelope.payload() == null
                || envelope.payload().solicitacaoId() == null) {

            throw new IllegalArgumentException(
                    "Envelope de cancelamento inválido"
            );
        }

        UUID solicitacaoId = envelope.payload().solicitacaoId();
        UUID correlationId = envelope.correlationId();

        // Serializa cancelamento e criação da mesma solicitação.
        solicitacaoLockService.bloquear(solicitacaoId);

        if (idempotenciaService.validarMensagemProcessada(
                envelope.messageId())) {
            return;
        }

        idempotenciaService.registrarMensagem(envelope.messageId());

        Optional<Compromisso> resultado =
                compromissoRepository.findBySolicitacaoId(solicitacaoId);

        if (resultado.isEmpty()) {

            if (!cancelamentoAntecipadoRepository
                    .existsBySolicitacaoId(solicitacaoId)) {

                Instant agora = Instant.now();

                CancelamentoAntecipado cancelamento =
                        new CancelamentoAntecipado();

                cancelamento.setSolicitacaoId(solicitacaoId);
                cancelamento.setCorrelationId(correlationId);
                cancelamento.setCriadoEm(agora);

                cancelamentoAntecipadoRepository.save(cancelamento);

                // Contrato: compromissoId pode ser null
                // somente no cancelamento antecipado.
                CompromissoCanceladoEvent evento =
                        new CompromissoCanceladoEvent(
                                solicitacaoId,
                                null,
                                "CANCELAMENTO_ANTECIPADO",
                                agora
                        );

                outboxService.registrar(
                        correlationId,
                        "CompromissoCancelado",
                        evento
                );
            }

            return;
        }

        Compromisso compromisso = resultado.get();

        if (compromisso.getStatus() == StatusCompromisso.CANCELADO) {
            return;
        }

        if (compromisso.getStatus() == StatusCompromisso.RECUSADO) {
            return;
        }

        if (compromisso.getStatus() != StatusCompromisso.EM_ANALISE
                && compromisso.getStatus() != StatusCompromisso.ACEITO) {

            throw new IllegalStateException(
                    "Cancelamento inválido para status: "
                            + compromisso.getStatus()
            );
        }

        Instant agora = Instant.now();

        compromisso.setStatus(StatusCompromisso.CANCELADO);
        compromisso.setCanceladoEm(agora);
        compromisso.setAtualizadoEm(agora);

        compromissoRepository.save(compromisso);

        CompromissoCanceladoEvent evento =
                new CompromissoCanceladoEvent(
                        compromisso.getSolicitacaoId(),
                        compromisso.getIdCompromisso(),
                        "CANCELAMENTO_SOLICITADO",
                        agora
                );

        outboxService.registrar(
                correlationId,
                "CompromissoCancelado",
                evento
        );
    }

    @Transactional(readOnly = true)
    public Compromisso buscarCompromissoPorId(UUID compromissoId) {
        return compromissoRepository
                .findById(compromissoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Compromisso não encontrado: " + compromissoId
                        )
                );
    }
}
