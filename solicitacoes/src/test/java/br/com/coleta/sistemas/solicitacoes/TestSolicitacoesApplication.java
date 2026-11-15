package br.com.coleta.sistemas.solicitacoes;

import org.springframework.boot.SpringApplication;

public class TestSolicitacoesApplication {

	public static void main(String[] args) {
		SpringApplication.from(SolicitacoesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
