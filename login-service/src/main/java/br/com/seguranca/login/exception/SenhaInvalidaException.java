package br.com.seguranca.login.exception;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException() {
        super("Senha atual invalida");
    }
}
