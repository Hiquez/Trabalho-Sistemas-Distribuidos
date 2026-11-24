package br.com.coleta.sistemas.agendaecapacidade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendaecapacidadeApplication {

	public static void main(String[] args) {

        SpringApplication.run(AgendaecapacidadeApplication.class, args);
	}
}
