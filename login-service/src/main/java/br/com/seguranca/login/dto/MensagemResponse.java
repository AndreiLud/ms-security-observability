package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Mensagem", description = "Mensagem de retorno da operacao")
public record MensagemResponse(

        @Schema(description = "Descricao do resultado da operacao", example = "Login efetuado com sucesso")
        String mensagem) {
}
