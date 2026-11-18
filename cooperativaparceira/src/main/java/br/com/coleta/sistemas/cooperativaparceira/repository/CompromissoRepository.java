package br.com.coleta.sistemas.cooperativaparceira.repository;

import aj.org.objectweb.asm.commons.Remapper;
import br.com.coleta.sistemas.cooperativaparceira.Entity.Compromisso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.Optional;
import java.util.UUID;

public interface CompromissoRepository extends JpaRepository<Compromisso, UUID> {
    Optional<Compromisso> findByIdCompromisso(UUID uuid);

    Optional<Compromisso> findBySolicitacaoId(UUID uuid);
}
