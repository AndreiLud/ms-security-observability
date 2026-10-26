package br.com.seguranca.login.config;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguranca dos end-points.
 *
 * <ul>
 * <li>POST /login e PUT /trocarsenha/{userid} - acesso publico;</li>
 * <li>POST / e GET / - somente ROLE_MANUTENCAO;</li>
 * <li>PUT /unlock/{userid} - somente ROLE_ADMIN.</li>
 * </ul>
 *
 * A consistencia de usuarios e roles e feita pelo
 * {@link JdbcUserDetailsManager}, que utiliza as tabelas <code>users</code> e
 * <code>authorities</code> criadas em <code>schema.sql</code>.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // permite o console do H2 renderizar dentro de frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // end-points publicos
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/trocarsenha/**").permitAll()
                        // end-points protegidos por role
                        .requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
                        .requestMatchers(HttpMethod.PUT, "/unlock/**").hasRole("ADMIN")
                        // documentacao OPENAPI / Swagger
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // indicadores consumidos pelo Prometheus
                        .requestMatchers("/actuator/**").permitAll()
                        // console do banco H2
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(withDefaults())
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JdbcUserDetailsManager userDetailsService(DataSource datasource) {
        return new JdbcUserDetailsManager(datasource);
    }
}
