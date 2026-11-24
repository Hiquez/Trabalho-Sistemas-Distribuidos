package br.com.coleta.sistemas.agendaecapacidade.Controller;

import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaRequestsDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.AgendaResponseDTO;
import br.com.coleta.sistemas.agendaecapacidade.Dto.CapacidadeAgendaRequestDTO;
import br.com.coleta.sistemas.agendaecapacidade.Entity.CapacidadeAgenda;
import br.com.coleta.sistemas.agendaecapacidade.Service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;

    @PostMapping("/reservar")
    public ResponseEntity<AgendaResponseDTO> reservar(@RequestBody AgendaRequestsDTO dto) {

        AgendaResponseDTO resposta = agendaService.reservar(dto);

        return ResponseEntity.accepted().body(resposta);
    }

    @PostMapping("/capacidade")
    public ResponseEntity<CapacidadeAgenda> cadastrarCapacidade(@RequestBody CapacidadeAgendaRequestDTO dto) {
        CapacidadeAgenda resposta = agendaService.cadastrarCapacidade(dto);
        return ResponseEntity.status(201).body(resposta);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> tratarErroDeRegraDeNegocio(IllegalStateException erro) {
        return ResponseEntity.status(409).body(erro.getMessage());
    }
}
