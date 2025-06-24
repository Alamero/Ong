package com.pingas.pingas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Chama index.html que está na pasta templates
    }

    @GetMapping("/cadastro")
    public String login() {
        return "cadastro"; // login.html
    }

    @GetMapping("/doador")
    public String doador() {
        return "doador"; // doador.html
    }

    @GetMapping("/ong")
    public String ong() {
        return "ong"; // ong.html
    }
    @GetMapping("/Doações")
    public String doacoes() {
        return "doador_doacoes"; // ong.html
    }
    @GetMapping("/Histórico")
    public String historicodoador() {
        return "doador_historico"; // ong.html
    }
    @GetMapping("/ONGs")
    public String ONGs() {
        return "doador_ongs"; // ong.html
    }
    @GetMapping("/Validar")
    public String Validar() {
        return "ong_Validar"; // ong.html
    }
    @GetMapping("/HistoricoOng")
    public String HistoricoOng() {
        return "ong_historico"; // ong.html
}
@GetMapping("/Dados")
    public String Dados() {
        return "ong_dados"; // ong.html
    }
    @GetMapping("/detalhes")
    public String detalhes() {
        return "detalhes"; // ong.html
}
@GetMapping("/doador_dados")
    public String doador_dados() {
        return "doador_dados"; // ong.html
    }
}