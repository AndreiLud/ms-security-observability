package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Usuario", description = "Usuario cadastrado e sua situacao de bloqueio")
public record UsuarioResponse(

        @Schema(description = "Nome do usuario", example = "joao")
        String username,

        @Schema(description = "Indica se o usuario esta bloqueado", example = "false")
        boolean bloqueado,

        @Schema(description = "Quantidade de tentativas de login invalidas consecutivas", example = "0")
        int tentativasInvalidas) {
}
