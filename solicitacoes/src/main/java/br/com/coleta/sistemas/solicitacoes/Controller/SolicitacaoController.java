package br.com.coleta.sistemas.solicitacoes.Controller;

import br.com.coleta.sistemas.solicitacoes.Dto.SolicitacaoRequestDTO;
import br.com.coleta.sistemas.solicitacoes.Dto.SolicitacaoResponseDTO;
import br.com.coleta.sistemas.solicitacoes.Service.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoResponseDTO> criar (@RequestBody SolicitacaoRequestDTO dto) {
        SolicitacaoResponseDTO resposta = solicitacaoService.criarSolicitacao(dto);

        return ResponseEntity.accepted().body(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable UUID id) {
        SolicitacaoResponseDTO resposta = solicitacaoService.buscarPorId(id);

        return ResponseEntity.ok(resposta);

    }
}
