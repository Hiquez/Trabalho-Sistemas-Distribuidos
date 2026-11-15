package br.com.coleta.sistemas.cooperativaparceira.repository;

import br.com.coleta.sistemas.cooperativaparceira.Entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CompromissoRepository extends JpaRepository<Compromisso, UUID> {
}
