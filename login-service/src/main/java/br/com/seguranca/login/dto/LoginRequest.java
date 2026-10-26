package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest", description = "Credenciais informadas no POST /login")
public record LoginRequest(

        @Schema(description = "Nome do usuario", example = "joao", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "username e obrigatorio")
        String username,

        @Schema(description = "Senha do usuario", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "senha e obrigatoria")
        @Size(max = 10, message = "senha deve conter no maximo 10 caracteres")
        String senha) {
}
