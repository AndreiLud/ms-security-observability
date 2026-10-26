package br.com.seguranca.login.metrics;

import org.springframework.stereotype.Component;

import br.com.seguranca.login.repository.LoginAttemptsRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Indicadores (metricas) exigidos pela atividade, expostos em
 * <code>/actuator/prometheus</code>:
 *
 * <ul>
 * <li><code>logins_validos_total</code> - quantidade de logins validos</li>
 * <li><code>logins_invalidos_total</code> - quantidade de logins invalidos</li>
 * <li><code>usuarios_bloqueados</code> - total de usuarios bloqueados</li>
 * </ul>
 */
@Component
public class LoginMetrica {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetrica(MeterRegistry registry, LoginAttemptsRepository loginAttempts) {

        this.loginsValidos = Counter.builder("logins.validos")
                .description("Quantidade de logins validos")
                .register(registry);

        this.loginsInvalidos = Counter.builder("logins.invalidos")
                .description("Quantidade de logins invalidos")
                .register(registry);

        // Gauge consultado sob demanda diretamente na tabela login_attempts
        Gauge.builder("usuarios.bloqueados", loginAttempts, LoginAttemptsRepository::contarBloqueados)
                .description("Total de usuarios bloqueados")
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido() {
        loginsInvalidos.increment();
    }
}
