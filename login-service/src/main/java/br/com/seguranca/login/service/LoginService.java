package br.com.seguranca.login.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.seguranca.login.dto.UsuarioResponse;
import br.com.seguranca.login.exception.SenhaInvalidaException;
import br.com.seguranca.login.exception.UsuarioJaExisteException;
import br.com.seguranca.login.exception.UsuarioNaoEncontradoException;
import br.com.seguranca.login.metrics.LoginMetrica;
import br.com.seguranca.login.repository.LoginAttemptsRepository;
import br.com.seguranca.login.repository.UsuarioRepository;

/**
 * Regras de negocio do servico de login.
 *
 * <p>
 * Duas bases de usuarios convivem no servico, cada uma com um proposito:
 * <ul>
 * <li><b>usuarios</b> - tabela do enunciado, consultada pelo POST /login e pelo
 * PUT /trocarsenha/{userid};</li>
 * <li><b>users / authorities</b> - schema do {@link JdbcUserDetailsManager},
 * usado pelo Spring Security para autenticar (HTTP Basic) e autorizar por role
 * os end-points protegidos.</li>
 * </ul>
 */
@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UsuarioRepository usuarios;
    private final LoginAttemptsRepository tentativas;
    private final LoginMetrica metrica;
    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final int maxTentativas;

    public LoginService(UsuarioRepository usuarios,
            LoginAttemptsRepository tentativas,
            LoginMetrica metrica,
            JdbcUserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder,
            @Value("${seguranca.login.max-tentativas:2}") int maxTentativas) {

        this.usuarios = usuarios;
        this.tentativas = tentativas;
        this.metrica = metrica;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.maxTentativas = maxTentativas;
    }

    /**
     * Consiste usuario e senha contra a tabela <code>usuarios</code>,
     * contabiliza os indicadores e aplica a regra de bloqueio.
     */
    @Transactional
    public ResultadoLogin autenticar(String username, String senha) {

        if (tentativas.estaBloqueado(username)) {
            metrica.loginInvalido();
            log.debug("Login recusado, usuario bloqueado: {}", username);
            return ResultadoLogin.BLOQUEADO;
        }

        Optional<String> senhaCadastrada = usuarios.obterSenha(username);

        if (senhaCadastrada.isPresent() && senhaCadastrada.get().equals(senha)) {
            tentativas.registrarSucesso(username);
            metrica.loginValido();
            log.debug("Login valido: {}", username);
            return ResultadoLogin.VALIDO;
        }

        // Apenas usuarios existentes sao contabilizados em login_attempts
        if (usuarios.existe(username)) {
            tentativas.registrarFalha(username, maxTentativas);
        }

        metrica.loginInvalido();
        log.debug("Login invalido: {}", username);
        return ResultadoLogin.INVALIDO;
    }

    /**
     * Cadastra o usuario na tabela <code>usuarios</code> e tambem na base de
     * seguranca do Spring Security, por meio do
     * {@link JdbcUserDetailsManager}, com a role padrao ROLE_USER.
     */
    @Transactional
    public void cadastrar(String username, String senha) {

        if (usuarios.existe(username) || userDetailsManager.userExists(username)) {
            throw new UsuarioJaExisteException(username);
        }

        usuarios.inserir(username, senha);

        UserDetails novo = User.withUsername(username)
                .password(passwordEncoder.encode(senha))
                .roles("USER")
                .build();

        userDetailsManager.createUser(novo);

        log.debug("Usuario cadastrado: {}", username);
    }

    /**
     * Troca a senha do usuario consistindo a senha atual com a base de dados.
     */
    @Transactional
    public void trocarSenha(String userid, String atual, String nova) {

        String senhaCadastrada = usuarios.obterSenha(userid)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(userid));

        if (!senhaCadastrada.equals(atual)) {
            throw new SenhaInvalidaException();
        }

        usuarios.atualizarSenha(userid, nova);

        // Mantem a base do Spring Security sincronizada, quando aplicavel
        if (userDetailsManager.userExists(userid)) {
            UserDetails existente = userDetailsManager.loadUserByUsername(userid);
            userDetailsManager.updateUser(User.withUserDetails(existente)
                    .password(passwordEncoder.encode(nova))
                    .build());
        }

        log.debug("Senha alterada para o usuario: {}", userid);
    }

    /** Desbloqueia o usuario zerando suas tentativas invalidas. */
    @Transactional
    public void desbloquear(String userid) {

        if (!usuarios.existe(userid)) {
            throw new UsuarioNaoEncontradoException(userid);
        }

        tentativas.desbloquear(userid);

        log.debug("Usuario desbloqueado: {}", userid);
    }

    /** Lista os usuarios cadastrados e a respectiva situacao de bloqueio. */
    public List<UsuarioResponse> listar() {

        List<UsuarioResponse> lista = new ArrayList<>();

        for (String username : usuarios.listarUsernames()) {
            lista.add(new UsuarioResponse(username,
                    tentativas.estaBloqueado(username),
                    tentativas.obterTentativasInvalidas(username)));
        }

        return lista;
    }
}
