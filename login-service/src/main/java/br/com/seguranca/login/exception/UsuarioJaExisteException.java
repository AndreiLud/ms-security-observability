package br.com.seguranca.login.exception;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String username) {
        super("Usuario ja cadastrado: " + username);
    }
}
