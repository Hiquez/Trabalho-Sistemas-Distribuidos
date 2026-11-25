package br.com.coleta.sistemas.solicitacoes;
import br.com.coleta.sistemas.solicitacoes.Dto.Envelope;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ResultadoReservaConsumerTest {
 @Test void envelopeValido(){var e=Envelope.criar(UUID.randomUUID(),"ResultadoReserva",Map.of("reservada",true));assertDoesNotThrow(()->e.validar("ResultadoReserva"));assertThrows(IllegalArgumentException.class,()->e.validar("Outro"));}
}
