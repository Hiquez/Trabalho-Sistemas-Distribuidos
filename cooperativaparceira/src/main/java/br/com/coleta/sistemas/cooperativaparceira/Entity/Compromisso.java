package br.com.coleta.sistemas.cooperativaparceira.Entity;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusCompromisso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idCompromisso;
    private UUID idSolicitante;
    private StatusCompromisso status;
    private Instant criadoEm;
    private Instant atualizadoEm;
    private Instant canceladoEm;
}
