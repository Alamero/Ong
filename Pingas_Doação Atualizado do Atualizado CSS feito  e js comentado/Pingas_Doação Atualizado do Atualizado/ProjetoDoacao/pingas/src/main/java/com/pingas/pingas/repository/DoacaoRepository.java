package com.pingas.pingas.repository;

import com.pingas.pingas.model.Doacao;
import com.pingas.pingas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findByDoador(User doador);

    List<Doacao> findByDoadorAndStatus(User doador, String status);

    List<Doacao> findByOng(User ong);

    List<Doacao> findByOngAndStatus(User ong, String status);
}
