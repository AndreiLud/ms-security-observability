package br.com.seguranca.login.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(String username) {
        super("Usuario nao encontrado: " + username);
    }
}
