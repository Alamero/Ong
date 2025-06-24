package com.pingas.pingas.controller;

import com.pingas.pingas.dto.DoacaoDTO;
import com.pingas.pingas.dto.StatusDTO;
import com.pingas.pingas.model.Doacao;
import com.pingas.pingas.model.User;
import com.pingas.pingas.repository.UserRepository;
import com.pingas.pingas.service.DoacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doacoes")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> criarDoacao(@RequestBody DoacaoDTO dto) {
        Optional<User> doadorOpt = userRepository.findByLogin(dto.getLoginDoador());
        if (doadorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Doador com login informado não encontrado.");
        }

        Optional<User> ongOpt = userRepository.findById(dto.getIdOng());
        if (ongOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("ONG com ID informado não encontrada.");
        }

        Doacao doacao = new Doacao();
        doacao.setDescricao(dto.getDescricao());
        doacao.setDoador(doadorOpt.get());
        doacao.setOng(ongOpt.get());
        doacao.setData(LocalDateTime.now());
        doacao.setStatus("PENDENTE");

        Doacao criada = doacaoService.criarDoacao(doacao);
        return ResponseEntity.ok(criada);
    }

    @GetMapping("/doador/historico")
public ResponseEntity<List<DoacaoDTO>> listarTodasDoacoesDoador() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String login = auth.getName();
    Optional<User> doadorOpt = userRepository.findByLogin(login);
    if (doadorOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    List<Doacao> doacoes = doacaoService.listarDoacoesPorDoador(doadorOpt.get());

    List<DoacaoDTO> dtos = doacoes.stream().map(doacao -> {
        DoacaoDTO dto = new DoacaoDTO();
        dto.setDescricao(doacao.getDescricao());
        dto.setLoginDoador(doacao.getDoador().getLogin());
        dto.setIdOng(doacao.getOng().getId());
        dto.setNomeOng(doacao.getOng().getNome());
        dto.setData(doacao.getData());
        dto.setStatus(doacao.getStatus().toString());
        dto.setMotivoRecusa(doacao.getMotivoRecusa());
        return dto;
    }).toList();

    return ResponseEntity.ok(dtos);
}


    @GetMapping("/doador/pendentes")
public ResponseEntity<List<DoacaoDTO>> listarDoacoesPendentesDoador() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String login = auth.getName();
    Optional<User> doadorOpt = userRepository.findByLogin(login);
    if (doadorOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    List<Doacao> pendentes = doacaoService.listarDoacoesPendentesPorDoador(doadorOpt.get());

    List<DoacaoDTO> dtos = pendentes.stream().map(doacao -> {
        DoacaoDTO dto = new DoacaoDTO();
        dto.setDescricao(doacao.getDescricao());
        dto.setLoginDoador(doacao.getDoador().getLogin());
        dto.setIdOng(doacao.getOng().getId());
        dto.setNomeOng(doacao.getOng().getNome());
        dto.setData(doacao.getData());
        dto.setStatus(doacao.getStatus().toString());
        dto.setMotivoRecusa(doacao.getMotivoRecusa());
        return dto;
    }).toList();

    return ResponseEntity.ok(dtos);
}


    // ✅ Atualizado para retornar dados simplificados
    @GetMapping("/pendentes")
    public ResponseEntity<List<Map<String, Object>>> listarDoacoesPendentesPorOng() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String login = auth.getName();
        Optional<User> ongOpt = userRepository.findByLogin(login);
        if (ongOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Doacao> pendentes = doacaoService.listarDoacoesPendentesPorOng(ongOpt.get());

        List<Map<String, Object>> resultado = pendentes.stream().map(doacao -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doacao.getId());
            map.put("nomeDoador", doacao.getDoador().getNome());
            map.put("data", doacao.getData());
            map.put("descricao", doacao.getDescricao());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/{id}/status")
public ResponseEntity<?> atualizarStatusComMotivo(@PathVariable Long id, @RequestBody StatusDTO dto) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String login = auth.getName();
    Optional<User> ongOpt = userRepository.findByLogin(login);
    if (ongOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ✅ Validação: se o status for NEGADO, o motivo é obrigatório
    if ("NEGADO".equalsIgnoreCase(dto.getStatus()) &&
        (dto.getMotivoRecusa() == null || dto.getMotivoRecusa().trim().isEmpty())) {
        return ResponseEntity.badRequest().body("Motivo da recusa é obrigatório.");
    }

    Optional<Doacao> doacaoOpt = doacaoService.atualizarStatusPorOng(id, dto.getStatus(), dto.getMotivoRecusa(), ongOpt.get());
    return doacaoOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
}
@GetMapping("/historico")
public ResponseEntity<List<Map<String, Object>>> listarHistoricoPorOng() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String login = auth.getName();
    Optional<User> ongOpt = userRepository.findByLogin(login);
    if (ongOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    List<Doacao> doacoes = doacaoService.listarDoacoesPorOng(ongOpt.get());

     List<Map<String, Object>> resultado = doacoes.stream().map(doacao -> {
        Map<String, Object> map = new HashMap<>();
        map.put("nomeDoador", doacao.getDoador().getNome());
        map.put("data", doacao.getData());
        map.put("status", doacao.getStatus());
        map.put("motivoRecusa", doacao.getMotivoRecusa()); // ⬅️ necessário aqui
        return map;
    }).collect(Collectors.toList());

    return ResponseEntity.ok(resultado);
}

}