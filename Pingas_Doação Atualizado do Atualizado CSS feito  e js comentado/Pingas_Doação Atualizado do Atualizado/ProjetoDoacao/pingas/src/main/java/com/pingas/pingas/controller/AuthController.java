package com.pingas.pingas.controller;

import com.pingas.pingas.model.User;
import com.pingas.pingas.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String login,
                                   @RequestParam String senha,
                                   HttpServletRequest request) {

        Optional<User> userOpt = userService.login(login, senha);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Login ou senha inválidos"));
        }

        User user = userOpt.get();

        String role = "ROLE_" + user.getTipo().toUpperCase();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getLogin(),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Login bem-sucedido",
                "nome", user.getNome(),
                "tipo", user.getTipo()
        ));
    }

 @PostMapping("/register/doador")
public ResponseEntity<?> registerDoador(@RequestBody Map<String, Object> payload) {
    try {
        // Extrai os campos
        String nome = (String) payload.get("nome");
        String login = (String) payload.get("login");
        String senha = (String) payload.get("senha");
        String cep = (String) payload.get("cep");
        String endereco = (String) payload.get("endereco");

        // ⬇️ Adiciona: pegar dataNascimento
        String dataNascimentoStr = (String) payload.get("dataNascimento");

        // Validação: login (CPF) deve ter 11 dígitos
        if (login == null || login.length() != 11) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "CPF inválido!"));
        }

        // Validação: senha >= 8 caracteres
        if (senha == null || senha.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Senha não tem mais de 8 dígitos"));
        }

        // Validação: dataNascimento obrigatório
        if (dataNascimentoStr == null || dataNascimentoStr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Data de nascimento é obrigatória!"));
        }

        // ⬇️ Converte para LocalDate
        LocalDate dataNascimento;
        try {
            dataNascimento = LocalDate.parse(dataNascimentoStr);  // formato yyyy-MM-dd
        } catch (DateTimeParseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Formato de data inválido! Use yyyy-MM-dd."));
        }

        // ⬇️ Valida idade >= 18 anos
        Period idade = Period.between(dataNascimento, LocalDate.now());
        if (idade.getYears() < 18) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Para utilizar desse deve-se ter mais de 18 anos."));
        }

        // Se passou, cadastra normalmente
        userService.register(
                nome,
                login,
                senha,
                "DOADOR",
                cep,
                endereco,
                null,
                null
        );

        return ResponseEntity.ok(Map.of("message", "Doador registrado com sucesso"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}

    // ✅ Cadastro de ONG via multipart/form-data
    @PostMapping(value = "/register/ong", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> registerOng(
        @RequestParam String nome,
        @RequestParam String login,
        @RequestParam String senha,
        @RequestParam String cep,
        @RequestParam String endereco,
        @RequestParam(required = false) MultipartFile imagem
) {
    try {
    if (login == null || login.length() != 14) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "CNPJ não tem 14 dígitos"));
    }

    if (senha == null || senha.length() < 8) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Senha não tem mais de 8 dígitos."));
    }

    byte[] imagemBytes = (imagem != null && !imagem.isEmpty()) ? imagem.getBytes() : null;

    userService.register(
            nome, login, senha, "ONG", cep, endereco, null, imagemBytes // numero = null
    );

    return ResponseEntity.ok(Map.of("message", "ONG registrada com sucesso"));
} catch (Exception e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", e.getMessage()));
}


}


    @GetMapping("/me")
    public ResponseEntity<?> usuarioLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuário não está autenticado"));
        }

        String login = auth.getName();
        Optional<User> userOpt = userService.findByLogin(login);

        return userOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuário não encontrado")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }

@PutMapping(value = "/atualizar-ong", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> atualizarDadosOng(
        @RequestParam String cep,
        @RequestParam String endereco,
        @RequestParam(required = false) MultipartFile imagem
) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Usuário não está autenticado"));
    }

    String login = auth.getName();
    Optional<User> userOpt = userService.findByLogin(login);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Usuário não encontrado"));
    }

    try {
        User user = userOpt.get();

        user.setCep(cep);
        user.setEndereco(endereco);

        if (imagem != null && !imagem.isEmpty()) {
            user.setImagem(imagem.getBytes());
        }

        userService.atualizarDados(user);

        return ResponseEntity.ok(Map.of("message", "Dados atualizados com sucesso"));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro ao atualizar dados"));
    }
}
@GetMapping("/usuario/logado")
public ResponseEntity<?> getUsuarioLogado(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String login = authentication.getName();
    Optional<User> userOpt = userService.findByLogin(login);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", "Usuário não encontrado"));
    }

    User user = userOpt.get();

    Map<String, Object> response = Map.of(
        "nome", user.getNome(),
        "tipo", user.getTipo(),
        "cep", user.getCep(),
        "endereco", user.getEndereco()
    );

    return ResponseEntity.ok(response);
}


}
