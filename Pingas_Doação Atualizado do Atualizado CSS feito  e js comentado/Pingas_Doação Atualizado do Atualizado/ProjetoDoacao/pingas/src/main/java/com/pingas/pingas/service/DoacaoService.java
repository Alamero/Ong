package com.pingas.pingas.service;

import com.pingas.pingas.model.Doacao;
import com.pingas.pingas.model.User;
import com.pingas.pingas.repository.DoacaoRepository;
import com.pingas.pingas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoacaoService {

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private UserRepository userRepository;

    // Lista todas as doações feitas por um doador
    public List<Doacao> listarDoacoesPorDoador(User doador) {
        return doacaoRepository.findByDoador(doador);
    }

    // Lista apenas as doações pendentes feitas por um doador
    public List<Doacao> listarDoacoesPendentesPorDoador(User doador) {
        return doacaoRepository.findByDoadorAndStatus(doador, "PENDENTE");
    }

    // Cria uma nova doação
    public Doacao criarDoacao(Doacao doacao) {
        doacao.setStatus("PENDENTE");
        doacao.setData(LocalDateTime.now());
        return doacaoRepository.save(doacao);
    }

    // Atualiza o status da doação (geral, sem motivo)
    public Optional<Doacao> atualizarStatus(Long id, String status) {
        Optional<Doacao> doacaoOpt = doacaoRepository.findById(id);
        if (doacaoOpt.isPresent()) {
            Doacao doacao = doacaoOpt.get();
            doacao.setStatus(status.toUpperCase());
            doacaoRepository.save(doacao);
            return Optional.of(doacao);
        }
        return Optional.empty();
    }

    public User buscarOngPorId(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Lista doações recebidas por uma ONG (todas)
    public List<Doacao> listarDoacoesPorOng(User ong) {
        return doacaoRepository.findByOng(ong);
    }

    // Lista doações da ONG com status específico
    public List<Doacao> listarDoacoesPorOngEStatus(User ong, String status) {
        return doacaoRepository.findByOngAndStatus(ong, status.toUpperCase());
    }

    // ✅ Atalho para listar apenas pendentes da ONG
    public List<Doacao> listarDoacoesPendentesPorOng(User ong) {
        return listarDoacoesPorOngEStatus(ong, "PENDENTE");
    }

    // ✅ Atualiza o status da doação (com motivo se "NEGADO")
    public Optional<Doacao> atualizarStatusComMotivo(Long id, String status, String motivoRecusa) {
        Optional<Doacao> opt = doacaoRepository.findById(id);
        if (opt.isPresent()) {
            Doacao d = opt.get();
            d.setStatus(status.toUpperCase());
            d.setMotivoRecusa(status.equalsIgnoreCase("NEGADO") ? motivoRecusa : null);
            return Optional.of(doacaoRepository.save(d));
        }
        return Optional.empty();
    }

    // ✅ (opcional) Com validação da ONG responsável
    public Optional<Doacao> atualizarStatusPorOng(Long id, String status, String motivoRecusa, User ong) {
        Optional<Doacao> opt = doacaoRepository.findById(id);
        if (opt.isPresent()) {
            Doacao d = opt.get();

            if (d.getOng() == null || !d.getOng().getId().equals(ong.getId())) {
                return Optional.empty();
            }

            d.setStatus(status.toUpperCase());
            d.setMotivoRecusa(status.equalsIgnoreCase("NEGADO") ? motivoRecusa : null);
            return Optional.of(doacaoRepository.save(d));
        }
        return Optional.empty();
    }
}
