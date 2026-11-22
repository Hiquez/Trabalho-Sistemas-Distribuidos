package br.com.coleta.sistemas.cooperativaparceira.Entity;

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
@Table(name = "MENSAGEM_PROCESSADA")
public class MensagemProcessada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idMensagemProcessada;

    @Column(nullable = false, unique = true)
    private UUID messageId;

    private UUID correlationId;

    private UUID solicitacaoId;

    private String tipoMensagem;

    private Instant processadaEm;
}
