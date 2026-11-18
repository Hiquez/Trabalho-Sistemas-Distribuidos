package br.com.coleta.sistemas.cooperativaparceira.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulacaoCooperativaService {

    @Value("${cooperativa.simulacao.taxa-recusa:0.20}")
    private double taxaRecusa;

    public boolean deveAceitar() {
        return ThreadLocalRandom.current()
                .nextDouble() >= taxaRecusa;
    }
}
