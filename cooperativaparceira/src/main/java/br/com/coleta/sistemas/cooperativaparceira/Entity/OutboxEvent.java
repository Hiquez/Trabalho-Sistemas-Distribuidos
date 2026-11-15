package br.com.coleta.sistemas.cooperativaparceira.Entity;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class OutboxEvent {

    private UUID id;
    private UUID messageId;
    private UUID correlationId;
    private String tipoEvento;
    private String payload;
    private StatusOutbox status;
    private Instant criadoEm;
    private Instant publicadoEm;
}
