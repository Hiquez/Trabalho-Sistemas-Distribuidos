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
@Table
public class MensagemProcessada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idMensagemProcessada;
    private UUID messageId;
    private String tipoMensagem;
    private Instant processadaEm;
}
