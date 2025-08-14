package med.voll.api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import med.voll.api.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// @Service: Marca a classe como um serviço do Spring, especializada em lógica de negócio/serviços.
@Service
public class TokenService {

    // @Value: Injeta um valor do arquivo application.properties.
    // É a forma correta de lidar com segredos, em vez de deixá-los no código.
    @Value("${api.security.token.secret}")
    private String secret;

    // Método responsável por gerar o token JWT.
    public String gerarToken(Usuario usuario) {
        try {
            // Define o algoritmo de assinatura do token (HMAC256) usando o segredo.
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    // Define o "issuer" (emissor) do token.
                    .withIssuer("API Voll.med")
                    // Define o "subject" (assunto) do token, geralmente o identificador do usuário.
                    .withSubject(usuario.getLogin())
                    // Define a data de expiração do token.
                    .withExpiresAt(dataExpiracao())
                    // Assina o token, finalizando sua criação.
                    .sign(algoritmo);
        } catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    // Método responsável por validar o token e extrair o "subject".
    public String getSubject(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    // Verifica se o emissor do token é o mesmo que o esperado.
                    .withIssuer("API Voll.med")
                    // Constrói o objeto de verificação.
                    .build()
                    // Tenta verificar o token. Se for inválido ou expirado, lança uma exceção.
                    .verify(tokenJWT)
                    // Se a verificação for bem-sucedida, extrai o "subject".
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    // Método auxiliar para calcular a data de expiração (2 horas a partir de agora).
    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}