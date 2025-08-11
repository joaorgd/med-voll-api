package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgendaDeConsultas {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public void agendar(DadosAgendamentoConsulta dados) {

        if(!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente inexistente!");
        }

        if(dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Id do médico inexistente!");
        }

        var medico = esolherMedico(dados);
        var paciente = pacienteRepository.getReferenceById(dados.idPaciente());
        var consulta = new Consulta(null, medico, paciente, dados.data());
        consultaRepository.save(consulta);
    }

    private Medico esolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicoRepository.getReferenceById((dados.idMedico()));
        }

        if(dados.especialidade() == null) {
            throw new ValidacaoException("Quando o médico não for escolhido, a especialidade é obrigatória!");
        }

        return medicoRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());
    }
}
