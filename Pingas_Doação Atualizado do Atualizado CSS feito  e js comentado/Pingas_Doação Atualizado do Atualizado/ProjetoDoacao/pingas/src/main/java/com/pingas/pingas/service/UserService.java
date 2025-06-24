package com.pingas.pingas.service;

import com.pingas.pingas.model.User;
import com.pingas.pingas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String nome, String login, String password, String tipo, String cep, String endereco, String numero, byte[] imagem) throws Exception {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new Exception("Login já existe");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setNome(nome);
        user.setLogin(login);
        user.setSenha(hashedPassword);
        user.setTipo(tipo.toUpperCase());
        user.setCep(cep);
        user.setEndereco(endereco);
        user.setImagem(imagem);

        return userRepository.save(user);
    }

    public Optional<User> login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getSenha())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public List<User> listarOngs() {
        return userRepository.findByTipo("ONG");
    }

    
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
    public void atualizarDados(User user) {
    userRepository.save(user); // Assumindo que o User já está carregado e atualizado
}

public Optional<User> buscarPorId(Long id) {
    return userRepository.findById(id);
}


}
