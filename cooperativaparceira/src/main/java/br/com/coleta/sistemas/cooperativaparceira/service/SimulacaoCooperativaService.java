package br.com.coleta.sistemas.cooperativaparceira.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulacaoCooperativaService {

    @Value("${cooperativa.simulacao.taxa-recusa:0.20}")
    private double taxaRecusa;

    @Value("${cooperativa.simulacao.latencia-min-ms:100}")
    private long latenciaMinMs;

    @Value("${cooperativa.simulacao.latencia-max-ms:2000}")
    private long latenciaMaxMs;

    @Value("${cooperativa.simulacao.taxa-falha:0}")
    private double taxaFalha;

    public boolean deveAceitar() {
        return ThreadLocalRandom.current()
                .nextDouble() >= taxaRecusa;
    }

    public void simularLatencia() {
        if (latenciaMinMs < 0 || latenciaMaxMs < latenciaMinMs) {
            throw new IllegalStateException(
                    "Intervalo de latência inválido"
            );
        }

        long latencia = ThreadLocalRandom.current()
                .nextLong(latenciaMinMs, latenciaMaxMs + 1);

        try {
            Thread.sleep(latencia);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Simulação interrompida", e
            );
        }
    }

    public void simularFalha() {
        if (ThreadLocalRandom.current().nextDouble() < taxaFalha) {
            throw new IllegalStateException(
                    "Falha técnica simulada na cooperativa"
            );
        }
    }
}