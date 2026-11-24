package br.com.coleta.sistemas.solicitacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolicitacoesApplication {

	public static void main(String[] args) {

        SpringApplication.run(SolicitacoesApplication.class, args);
	}

}
