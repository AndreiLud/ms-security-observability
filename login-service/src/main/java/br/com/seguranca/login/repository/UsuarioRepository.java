package br.com.seguranca.login.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Acesso a tabela <code>usuarios</code> definida no enunciado da atividade.
 * E contra esta tabela que o POST /login consiste usuario e senha.
 */
@Repository
public class UsuarioRepository {

    private final JdbcClient jdbc;

    public UsuarioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<String> obterSenha(String username) {
        return jdbc.sql("SELECT senha FROM usuarios WHERE username = ?")
                .param(username)
                .query(String.class)
                .optional();
    }

    public boolean existe(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM usuarios WHERE username = ?")
                .param(username)
                .query(Integer.class)
                .single() > 0;
    }

    public void inserir(String username, String senha) {
        jdbc.sql("INSERT INTO usuarios (username, senha) VALUES (?, ?)")
                .params(username, senha)
                .update();
    }

    public int atualizarSenha(String username, String novaSenha) {
        return jdbc.sql("UPDATE usuarios SET senha = ? WHERE username = ?")
                .params(novaSenha, username)
                .update();
    }

    public List<String> listarUsernames() {
        return jdbc.sql("SELECT username FROM usuarios ORDER BY username")
                .query(String.class)
                .list();
    }
}
