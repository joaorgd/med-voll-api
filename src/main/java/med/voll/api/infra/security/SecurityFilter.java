package med.voll.api.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import med.voll.api.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component: Marca a classe como um componente genérico do Spring.
// OncePerRequestFilter: Garante que o filtro seja executado apenas uma vez por requisição.
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    // Método principal do filtro, que intercepta todas as requisições.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Recupera o token JWT do cabeçalho da requisição.
        var tokenJWT = recuperarToken(request);

        // Se um token foi enviado, valida-o.
        if (tokenJWT != null) {
            // Pede ao TokenService para validar o token e extrair o "subject" (login do usuário).
            var subject = tokenService.getSubject(tokenJWT);
            // Busca o usuário no banco de dados com base no login extraído.
            var usuario = repository.findByLogin(subject);

            // Cria um objeto de autenticação para o Spring Security.
            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

            // Define o usuário como autenticado no contexto de segurança do Spring para esta requisição.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continua o fluxo da requisição, passando para os próximos filtros e, finalmente, para o controller.
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extrair o token do cabeçalho "Authorization".
    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            // O token vem no formato "Bearer <token>", então removemos o prefixo.
            return authorizationHeader.replace("Bearer ", "");
        }

        // Retorna null se o cabeçalho não for encontrado.
        return null;
    }
}