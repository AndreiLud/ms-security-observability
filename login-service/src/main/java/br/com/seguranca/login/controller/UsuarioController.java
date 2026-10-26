package br.com.seguranca.login.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.seguranca.login.dto.LoginRequest;
import br.com.seguranca.login.dto.MensagemResponse;
import br.com.seguranca.login.dto.NovoUsuarioRequest;
import br.com.seguranca.login.dto.TrocaSenhaRequest;
import br.com.seguranca.login.dto.UsuarioResponse;
import br.com.seguranca.login.service.LoginService;
import br.com.seguranca.login.service.ResultadoLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuarios", description = "Login, cadastro, troca de senha e desbloqueio de usuarios")
public class UsuarioController {

    private final LoginService loginService;

    public UsuarioController(LoginService loginService) {
        this.loginService = loginService;
    }

    // -----------------------------------------------------------------------
    // POST /login - acesso publico
    // -----------------------------------------------------------------------
    @PostMapping("/login")
    @Operation(summary = "Efetua o login de um usuario",
            description = "Consiste usuario e senha contra a tabela usuarios. "
                    + "End-point de acesso publico. Apos mais de uma tentativa invalida "
                    + "o usuario e bloqueado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario e senha validos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuario e senha invalidos ou usuario bloqueado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corpo da requisicao invalido",
                    content = @Content) })
    public ResponseEntity<MensagemResponse> login(@Valid @RequestBody LoginRequest requisicao) {

        ResultadoLogin resultado = loginService.autenticar(requisicao.username(), requisicao.senha());

        return switch (resultado) {
            case VALIDO -> ResponseEntity.ok(new MensagemResponse("Login efetuado com sucesso"));
            case BLOQUEADO -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuario bloqueado por excesso de tentativas invalidas"));
            case INVALIDO -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuario ou senha invalidos"));
        };
    }

    // -----------------------------------------------------------------------
    // PUT /trocarsenha/{userid} - acesso publico
    // -----------------------------------------------------------------------
    @PutMapping("/trocarsenha/{userid}")
    @Operation(summary = "Troca a senha de um usuario",
            description = "Consiste a senha atual com a base de dados e grava a nova senha. "
                    + "End-point de acesso publico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Senha atual invalida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Corpo da requisicao invalido", content = @Content) })
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "Usuario que tera a senha alterada", example = "joao")
            @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest requisicao) {

        loginService.trocarSenha(userid, requisicao.atual(), requisicao.nova());

        return ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso"));
    }

    // -----------------------------------------------------------------------
    // POST / - somente ROLE_MANUTENCAO
    // -----------------------------------------------------------------------
    @PostMapping("/")
    @Operation(summary = "Cadastra um novo usuario",
            description = "Somente usuarios com a role ROLE_MANUTENCAO podem executar esta operacao. "
                    + "O usuario e gravado na tabela usuarios e tambem na base de seguranca "
                    + "do Spring Security (JdbcUserDetailsManager) com a role ROLE_USER.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuario ja cadastrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Corpo da requisicao invalido", content = @Content) })
    public ResponseEntity<MensagemResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest requisicao) {

        loginService.cadastrar(requisicao.username(), requisicao.senha());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensagemResponse("Usuario cadastrado: " + requisicao.username()));
    }

    // -----------------------------------------------------------------------
    // PUT /unlock/{userid} - somente ROLE_ADMIN
    // -----------------------------------------------------------------------
    @PutMapping("/unlock/{userid}")
    @Operation(summary = "Desbloqueia um usuario",
            description = "Zera as tentativas invalidas e remove o bloqueio do usuario. "
                    + "Somente usuarios com a role ROLE_ADMIN podem executar esta operacao.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desbloqueado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content) })
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "Usuario a ser desbloqueado", example = "joao")
            @PathVariable String userid) {

        loginService.desbloquear(userid);

        return ResponseEntity.ok(new MensagemResponse("Usuario desbloqueado: " + userid));
    }

    // -----------------------------------------------------------------------
    // GET / - somente ROLE_MANUTENCAO
    // -----------------------------------------------------------------------
    @GetMapping("/")
    @Operation(summary = "Lista os usuarios cadastrados",
            description = "Retorna todos os usuarios cadastrados em formato JSON. "
                    + "Somente usuarios com a role ROLE_MANUTENCAO podem executar esta operacao. "
                    + "A senha nao e exposta na resposta.",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios cadastrados",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario sem a role ROLE_MANUTENCAO", content = @Content) })
    public ResponseEntity<List<UsuarioResponse>> listar() {

        return ResponseEntity.ok(loginService.listar());
    }
}
