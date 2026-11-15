package br.com.coleta.sistemas.cooperativaparceira;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CooperativaparceiraApplicationTests {

	@Test
	void contextLoads() {
	}

}
