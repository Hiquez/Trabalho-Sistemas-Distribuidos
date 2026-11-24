package br.com.coleta.sistemas.agendaecapacidade.Entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "capacidades_agenda", uniqueConstraints = {@UniqueConstraint(columnNames = {"bairro", "data_coleta"})})
public class CapacidadeAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String bairro;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Column(name = "capacidade_total", nullable = false)
    private Integer capacidadeTotal;

    @Column(name = "vagas_reservadas", nullable = false)
    private Integer vagasReservadas;

    public CapacidadeAgenda() {

    }

    public CapacidadeAgenda(
            String bairro,
            LocalDate dataColeta,
            Integer capacidadeTotal
    ) {
        this.bairro = bairro;
        this.dataColeta = dataColeta;
        this.capacidadeTotal = capacidadeTotal;
        this.vagasReservadas = 0;
    }
}
