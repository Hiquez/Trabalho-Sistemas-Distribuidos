package br.com.coleta.sistemas.agendaecapacidade;

import org.springframework.boot.SpringApplication;

public class TestAgendaecapacidadeApplication {

	public static void main(String[] args) {
		SpringApplication.from(AgendaecapacidadeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
