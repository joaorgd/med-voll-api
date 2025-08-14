package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.consulta.AgendaDeConsultas;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.consulta.DadosCancelamentoConsulta;
import med.voll.api.domain.consulta.DadosDetalhamentoConsulta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("consultas")
@SecurityRequirement(name = "bearer-key")
public class ConsultaController {

    // Boa prática: O controller depende da camada de serviço (AgendaDeConsultas),
    // e não diretamente do repositório.
    @Autowired
    private AgendaDeConsultas agenda;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoConsulta> agendar(@RequestBody @Valid DadosAgendamentoConsulta dados, UriComponentsBuilder uriBuilder) {
        // Delega toda a lógica de negócio (validações, etc.) para o serviço.
        var dadosDetalhamento = agenda.agendar(dados);

        var uri = uriBuilder.path("/consultas/{id}").buildAndExpand(dadosDetalhamento.id()).toUri();
        // Retorna 201 Created, que é o correto para criação.
        return ResponseEntity.created(uri).body(dadosDetalhamento);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> cancelar(@RequestBody @Valid DadosCancelamentoConsulta dados) {
        // Delega a lógica de cancelamento para o serviço.
        agenda.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
}