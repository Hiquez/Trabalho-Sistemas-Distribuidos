package br.com.coleta.sistemas.cooperativaparceira.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class LatenciaService {

    @Value("${cooperativa.simulacao.latencia-min-ms}")
    private double taxaLatenciaMin;
    @Value("${cooperativa.simulacao.latencia-max-ms}")
    private double taxalatenciaMax;
    @Value("${cooperativa.simulacao.taxa-falha}")
    private boolean falhou;

    public double latenciaDaOperacao(){
        return  ThreadLocalRandom.current().nextDouble(taxaLatenciaMin, taxalatenciaMax);
    }

    public boolean falhou(){
        return falhou;
    }
}
