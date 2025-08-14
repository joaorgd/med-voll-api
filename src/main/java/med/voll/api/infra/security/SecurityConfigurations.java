package med.voll.api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration: Marca a classe como uma fonte de definições de beans (componentes) para o Spring.
@Configuration
// @EnableWebSecurity: Ativa as configurações de segurança web do Spring Security.
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    // @Bean: Expõe o método para o Spring, permitindo que ele gerencie o objeto retornado.
    // SecurityFilterChain: Define a cadeia de filtros de segurança que serão aplicados às requisições.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita a proteção contra CSRF (Cross-Site Request Forgery).
                // Como a autenticação será via token, essa proteção não é necessária.
                .csrf(csrf -> csrf.disable())
                // Configura a política de gerenciamento de sessão para STATELESS.
                // Isso significa que o servidor não guardará estado (sessão) do usuário.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Inicia a configuração de autorização para as requisições HTTP.
                .authorizeHttpRequests(req -> {
                    // Permite que requisições POST para /login sejam feitas sem autenticação.
                    req.requestMatchers(HttpMethod.POST, "/login").permitAll();
                    // Permite acesso à documentação do Swagger sem autenticação.
                    req.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                    // Exige que todas as outras requisições (`anyRequest`) sejam autenticadas.
                    req.anyRequest().authenticated();
                })
                // Adiciona nosso filtro personalizado (SecurityFilter) para ser executado
                // antes do filtro padrão de autenticação do Spring.
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Bean para expor o AuthenticationManager, necessário para o processo de autenticação.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Bean para definir o algoritmo de hash de senhas.
    // BCrypt é o padrão recomendado para armazenamento seguro de senhas.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}