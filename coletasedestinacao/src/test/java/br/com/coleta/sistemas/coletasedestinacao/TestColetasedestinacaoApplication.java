package br.com.coleta.sistemas.coletasedestinacao;

import org.springframework.boot.SpringApplication;

public class TestColetasedestinacaoApplication {

	public static void main(String[] args) {
		SpringApplication.from(ColetasedestinacaoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
