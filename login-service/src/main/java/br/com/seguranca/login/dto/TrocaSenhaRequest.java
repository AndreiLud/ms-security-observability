package br.com.seguranca.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "TrocaSenhaRequest", description = "Senha atual e nova senha informadas no PUT /trocarsenha/{userid}")
public record TrocaSenhaRequest(

        @Schema(description = "Senha atual do usuario", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "atual e obrigatoria")
        @Size(max = 10, message = "atual deve conter no maximo 10 caracteres")
        String atual,

        @Schema(description = "Nova senha do usuario", example = "654321", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "nova e obrigatoria")
        @Size(max = 10, message = "nova deve conter no maximo 10 caracteres")
        String nova) {
}
