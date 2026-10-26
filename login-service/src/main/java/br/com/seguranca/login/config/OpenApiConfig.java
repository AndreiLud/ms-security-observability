package br.com.seguranca.login.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Documentacao dos end-points no padrao OPENAPI.
 *
 * <ul>
 * <li>JSON: http://localhost:8080/v3/api-docs</li>
 * <li>YAML: http://localhost:8080/v3/api-docs.yaml</li>
 * <li>Swagger: http://localhost:8080/swagger-ui.html</li>
 * </ul>
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Login Service",
        version = "1.0.0",
        description = "Mecanismo de login RESTful com seguranca por roles (JdbcUserDetailsManager), "
                + "bloqueio por tentativas invalidas e indicadores expostos para o Prometheus."))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic",
        description = "Autenticacao HTTP Basic. Usuarios: admin/admin (ROLE_ADMIN) "
                + "e manutencao/manutencao (ROLE_MANUTENCAO).")
public class OpenApiConfig {
}
