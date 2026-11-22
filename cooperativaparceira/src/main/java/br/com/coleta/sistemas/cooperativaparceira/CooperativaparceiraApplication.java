package br.com.coleta.sistemas.cooperativaparceira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CooperativaparceiraApplication {

	public static void main(String[] args) {
		SpringApplication.run(CooperativaparceiraApplication.class, args);
	}

}
