package br.com.seguranca.login.service;

/** Resultado possivel de uma tentativa de login. */
public enum ResultadoLogin {

    /** Usuario e senha validos. */
    VALIDO,

    /** Usuario e/ou senha invalidos. */
    INVALIDO,

    /** Usuario bloqueado por excesso de tentativas invalidas. */
    BLOQUEADO
}
