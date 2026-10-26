package br.com.seguranca.login.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Acesso a tabela <code>login_attempts</code> definida no enunciado da
 * atividade. Controla a quantidade de tentativas invalidas consecutivas e a
 * situacao de bloqueio de cada usuario.
 */
@Repository
public class LoginAttemptsRepository {

    private final JdbcClient jdbc;

    public LoginAttemptsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean estaBloqueado(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM login_attempts WHERE username = ? AND locked = TRUE")
                .param(username)
                .query(Integer.class)
                .single() > 0;
    }

    public int obterTentativasInvalidas(String username) {
        return jdbc.sql("SELECT failed_attempts FROM login_attempts WHERE username = ?")
                .param(username)
                .query(Integer.class)
                .optional()
                .orElse(0);
    }

    /**
     * Registra uma tentativa de login invalida e bloqueia o usuario quando o
     * total de falhas consecutivas atinge <code>maxTentativas</code>.
     */
    public void registrarFalha(String username, int maxTentativas) {

        int atualizadas = jdbc.sql("""
                UPDATE login_attempts
                   SET failed_attempts = failed_attempts + 1,
                       last_failed_at  = CURRENT_TIMESTAMP,
                       updated_at      = CURRENT_TIMESTAMP
                 WHERE username = ?
                """)
                .param(username)
                .update();

        if (atualizadas == 0) {
            jdbc.sql("""
                    INSERT INTO login_attempts
                           (username, failed_attempts, locked, last_failed_at, updated_at)
                    VALUES (?, 1, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """)
                    .param(username)
                    .update();
        }

        jdbc.sql("""
                UPDATE login_attempts
                   SET locked     = TRUE,
                       locked_at  = CURRENT_TIMESTAMP,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE username = ?
                   AND failed_attempts >= ?
                   AND locked = FALSE
                """)
                .params(username, maxTentativas)
                .update();
    }

    /** Zera o contador de falhas apos um login valido. */
    public void registrarSucesso(String username) {
        jdbc.sql("""
                UPDATE login_attempts
                   SET failed_attempts = 0,
                       updated_at      = CURRENT_TIMESTAMP
                 WHERE username = ?
                   AND locked = FALSE
                """)
                .param(username)
                .update();
    }

    /** Desbloqueia o usuario e zera suas tentativas (PUT /unlock/{userid}). */
    public void desbloquear(String username) {
        int atualizadas = jdbc.sql("""
                UPDATE login_attempts
                   SET failed_attempts = 0,
                       locked          = FALSE,
                       locked_at       = NULL,
                       updated_at      = CURRENT_TIMESTAMP
                 WHERE username = ?
                """)
                .param(username)
                .update();

        if (atualizadas == 0) {
            jdbc.sql("""
                    INSERT INTO login_attempts
                           (username, failed_attempts, locked, updated_at)
                    VALUES (?, 0, FALSE, CURRENT_TIMESTAMP)
                    """)
                    .param(username)
                    .update();
        }
    }

    /** Indicador: total de usuarios atualmente bloqueados. */
    public long contarBloqueados() {
        return jdbc.sql("SELECT COUNT(*) FROM login_attempts WHERE locked = TRUE")
                .query(Long.class)
                .single();
    }
}
