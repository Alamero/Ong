package com.pingas.pingas.repository;

import com.pingas.pingas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    
    List<User> findByTipo(String tipo); // busca todos do tipo




}
