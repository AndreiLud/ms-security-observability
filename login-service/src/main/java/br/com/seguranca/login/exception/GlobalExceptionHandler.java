package br.com.seguranca.login.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.seguranca.login.dto.MensagemResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        Map<String, String> erros = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        return erros;
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<MensagemResponse> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MensagemResponse(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<MensagemResponse> handleUsuarioJaExiste(UsuarioJaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MensagemResponse(ex.getMessage()));
    }

    @ExceptionHandler(SenhaInvalidaException.class)
    public ResponseEntity<MensagemResponse> handleSenhaInvalida(SenhaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MensagemResponse(ex.getMessage()));
    }
}
