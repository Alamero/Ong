package com.pingas.pingas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "usuario")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login; // CPF para doador ou CNPJ para ONG

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String tipo; // DOADOR ou ONG

    // NOVOS CAMPOS
    @Column(nullable = true)
    private String cep;

    @Column(nullable = true)
    private String endereco;
    @Lob
    @Column(name = "imagem", columnDefinition = "MEDIUMBLOB")
    private byte[] imagem;
}
