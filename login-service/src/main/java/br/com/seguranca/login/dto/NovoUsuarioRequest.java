package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "NovoUsuarioRequest", description = "Dados do usuario a ser cadastrado no POST /")
public record NovoUsuarioRequest(

        @Schema(description = "Nome do usuario", example = "pedro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "username e obrigatorio")
        @Size(max = 50, message = "username deve conter no maximo 50 caracteres")
        String username,

        @Schema(description = "Senha do usuario", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "senha e obrigatoria")
        @Size(max = 10, message = "senha deve conter no maximo 10 caracteres")
        String senha) {
}
