package br.com.coleta.sistemas.agendaecapacidade.Service;

import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaRequestsDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaResponseDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.CapacidadeAgendaRequestDTO;
import br.com.coleta.sistemas.agendaecapacidade.Entity.Agenda;
import br.com.coleta.sistemas.agendaecapacidade.Entity.CapacidadeAgenda;
import br.com.coleta.sistemas.agendaecapacidade.Repository.AgendaRepository;
import br.com.coleta.sistemas.agendaecapacidade.Repository.CapacidadeAgendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private final CapacidadeAgendaRepository capacidadeAgendaRepository;
    private final AgendaRepository agendaRepository;

    @Transactional(noRollbackFor =  IllegalStateException.class)
    public AgendaResponseDTO reservar(AgendaRequestsDTO dto) {

        var reservaExistente = agendaRepository.findBySolicitacaoId(dto.solicitacaoId());

        if (reservaExistente.isPresent()) {
            return converterParaDTO(reservaExistente.get());
        }

        int vagasReservadas = capacidadeAgendaRepository.reservarVaga(
                dto.bairro(),
                dto.dataColeta()
        );

        if (vagasReservadas == 0) {
            throw new IllegalStateException(
                    "Não existem vagas disponíveis para essa data e bairro."
            );
        }

        Agenda agenda = new Agenda(
                dto.solicitacaoId(),
                dto.bairro(),
                dto.dataColeta()
        );

        Agenda salva = agendaRepository.save(agenda);

        return converterParaDTO(salva);
    }

    @Transactional
    public CapacidadeAgenda cadastrarCapacidade(CapacidadeAgendaRequestDTO dto) {

        if (capacidadeAgendaRepository
                .findByBairroAndDataColeta(dto.bairro(), dto.dataColeta())
                .isPresent()) {

            throw new IllegalStateException(
                    "Já existe uma capacidade cadastrada para esse bairro e data."
            );
        }

        CapacidadeAgenda capacidade = new CapacidadeAgenda(
                dto.bairro(),
                dto.dataColeta(),
                dto.capacidadeTotal()
        );

        return capacidadeAgendaRepository.save(capacidade);
    }

    private AgendaResponseDTO converterParaDTO(Agenda agenda) {

        return new AgendaResponseDTO(
                agenda.getId(),
                agenda.getSolicitacaoId(),
                agenda.getBairro(),
                agenda.getDataColeta(),
                agenda.getStatus(),
                agenda.getCriadoEm()
        );
    }
}
