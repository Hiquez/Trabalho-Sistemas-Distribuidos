package br.com.coleta.sistemas.cooperativaparceira;

import org.springframework.boot.SpringApplication;

public class TestCooperativaparceiraApplication {

	public static void main(String[] args) {
		SpringApplication.from(CooperativaparceiraApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
