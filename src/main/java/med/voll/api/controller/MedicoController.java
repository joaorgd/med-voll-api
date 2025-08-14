package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

// @RestController: Combina @Controller e @ResponseBody, indicando que os métodos
// retornarão dados diretamente no corpo da resposta (geralmente JSON).
@RestController
// @RequestMapping: Define o prefixo da URL para todos os endpoints neste controller.
@RequestMapping("/medicos")
// @SecurityRequirement: Anotação do Swagger para indicar que os endpoints
// nesta classe exigem autenticação (o "bearer-key" é definido na classe de configuração do Spring Doc).
@SecurityRequirement(name = "bearer-key")
public class MedicoController {

    // @Autowired: Realiza a injeção de dependência. O Spring fornecerá uma instância de MedicoRepository.
    @Autowired
    private MedicoRepository repository;

    // @PostMapping: Mapeia este método para requisições HTTP do tipo POST.
    @PostMapping
    // @Transactional: Garante que o método seja executado dentro de uma transação com o banco de dados.
    @Transactional
    // @RequestBody: Indica que os dados do médico virão do corpo da requisição.
    // @Valid: Pede ao Spring para aplicar as validações do Bean Validation definidas no DTO.
    // UriComponentsBuilder: Objeto injetado pelo Spring para ajudar a construir a URI de resposta.
    public ResponseEntity<DadosDetalhamentoMedico> cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder) {
        var medico = new Medico(dados);
        repository.save(medico);

        // Constrói a URI para o novo recurso criado. Ex: /medicos/123
        var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(medico.getId()).toUri();

        // Retorna o status HTTP 201 Created (convenção para criação)
        // e os dados do médico recém-criado no corpo da resposta.
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    // @GetMapping: Mapeia este método para requisições HTTP do tipo GET.
    @GetMapping
    // Pageable: Objeto injetado pelo Spring que contém informações de paginação e ordenação
    // vindas da URL (ex: ?size=10&page=0&sort=nome).
    public ResponseEntity<Page<DadosListagemMedico>> listar(Pageable paginacao) {
        // Busca os médicos ativos no banco, já aplicando a paginação.
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
        // Retorna o status HTTP 200 OK e a página de médicos no corpo da resposta.
        return ResponseEntity.ok(page);
    }

    // @PutMapping: Mapeia para requisições HTTP PUT.
    // "/{id}": Indica que a URL conterá um parâmetro dinâmico (o ID do médico).
    @PutMapping("/{id}")
    @Transactional
    // @PathVariable: Vincula o parâmetro {id} da URL à variável Long id do método.
    public ResponseEntity<DadosDetalhamentoMedico> atualizar(@PathVariable Long id, @RequestBody @Valid DadosAtualizacaoMedico dados){
        // Carrega a entidade do banco de dados. getReferenceById é otimizado para updates.
        var medico = repository.getReferenceById(id);
        // Chama o método na própria entidade para atualizar suas informações.
        medico.atualizarInformacoes(dados);

        // Retorna 200 OK com os dados atualizados.
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    // @DeleteMapping: Mapeia para requisições HTTP DELETE.
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id){
        var medico = repository.getReferenceById(id);
        // Realiza a exclusão lógica, apenas inativando o médico.
        medico.excluir();

        // Retorna o status HTTP 204 No Content, indicando sucesso sem corpo de resposta.
        return ResponseEntity.noContent().build();
    }

    // Endpoint para detalhar um médico específico.
    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoMedico> detalhar(@PathVariable Long id){
        var medico = repository.getReferenceById(id);
        // Retorna 200 OK com os dados completos do médico.
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }
}