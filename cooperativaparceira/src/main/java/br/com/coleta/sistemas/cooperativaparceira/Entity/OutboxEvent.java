package br.com.coleta.sistemas.cooperativaparceira.Entity;

import br.com.coleta.sistemas.cooperativaparceira.Utils.Enum.StatusOutbox;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OUTBOX_EVENT")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID messageId;

    @Column(nullable = false)
    private UUID correlationId;

    @Column(nullable = false)
    private String tipoEvento;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOutbox status;

    @Column(nullable = false)
    private Instant criadoEm;

    private Instant publicadoEm;
}