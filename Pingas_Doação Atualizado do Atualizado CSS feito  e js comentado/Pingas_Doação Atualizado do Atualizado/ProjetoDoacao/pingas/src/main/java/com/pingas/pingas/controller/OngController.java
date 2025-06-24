package com.pingas.pingas.controller;

import com.pingas.pingas.model.Doacao;
import com.pingas.pingas.model.User;
import com.pingas.pingas.service.DoacaoService;
import com.pingas.pingas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ongs")
public class OngController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoacaoService doacaoService;

    // Listar todas as ONGs
    @GetMapping
    public ResponseEntity<List<User>> listarOngs() {
        List<User> ongs = userService.listarOngs();
        return ResponseEntity.ok(ongs);
    }

    // Listar apenas doações PENDENTES recebidas pela ONG logada
    @GetMapping("/doacoes")
    public ResponseEntity<List<Doacao>> listarDoacoesPendentes(@AuthenticationPrincipal User usuarioLogado) {
        if (!usuarioLogado.getTipo().equalsIgnoreCase("ONG")) {
            return ResponseEntity.status(403).build(); // Apenas ONGs têm acesso
        }

        List<Doacao> pendentes = doacaoService.listarDoacoesPorOngEStatus(usuarioLogado, "PENDENTE");
        return ResponseEntity.ok(pendentes);
    }

    // Histórico de todas as doações recebidas pela ONG
    @GetMapping("/historico")
    public ResponseEntity<List<Doacao>> listarHistoricoDoacoes(@AuthenticationPrincipal User usuarioLogado) {
        if (!usuarioLogado.getTipo().equalsIgnoreCase("ONG")) {
            return ResponseEntity.status(403).build();
        }

        List<Doacao> historico = doacaoService.listarDoacoesPorOng(usuarioLogado);
        return ResponseEntity.ok(historico);
    }

    // Atualizar status da doação (ACEITA ou RECUSADA), com motivo opcional
    @PutMapping("/doacoes/{id}")
    public ResponseEntity<?> atualizarStatusDoacao(@PathVariable Long id,
                                                   @RequestParam String status,
                                                   @RequestParam(required = false) String motivoRecusa,
                                                   @AuthenticationPrincipal User usuarioLogado) {
        if (!usuarioLogado.getTipo().equalsIgnoreCase("ONG")) {
            return ResponseEntity.status(403).build();
        }

        Optional<Doacao> atualizada = doacaoService.atualizarStatusPorOng(id, status, motivoRecusa, usuarioLogado);
        if (atualizada.isPresent()) {
            return ResponseEntity.ok(atualizada.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // Buscar ONG pelo ID
@GetMapping("/{id}")
public ResponseEntity<User> buscarOngPorId(@PathVariable Long id) {
    Optional<User> ong = userService.buscarPorId(id);
    if (ong.isPresent() && "ONG".equalsIgnoreCase(ong.get().getTipo())) {
        return ResponseEntity.ok(ong.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

}
