
package br.com.coleta.sistemas.solicitacoes.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MensagemProcessadaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MensagemProcessadaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean registrarSeNovo(String messageId, String tipo) {

        int inseridos = jdbcTemplate.update("""
            INSERT INTO mensagens_processadas
                (message_id, tipo_mensagem, processado_em)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (message_id) DO NOTHING
            """, messageId, tipo);

        return inseridos == 1;
    }
}
