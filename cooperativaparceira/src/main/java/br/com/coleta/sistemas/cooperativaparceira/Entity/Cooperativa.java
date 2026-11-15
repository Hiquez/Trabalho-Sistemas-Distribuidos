package br.com.coleta.sistemas.cooperativaparceira.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cooperativa")
public class Cooperativa {

    private UUID idCooperativa;
    private String nomeCooperativa;
    private String cnpjCooperativa;

}
