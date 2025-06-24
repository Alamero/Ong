package com.pingas.pingas.dto;

import java.time.LocalDateTime;

public class DoacaoDTO {
    private String descricao;
    private String loginDoador;
    private Long idOng;
    private String nomeOng;
    private LocalDateTime data;
    private String status;
    private String motivoRecusa;

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLoginDoador() {
        return loginDoador;
    }
    public void setLoginDoador(String loginDoador) {
        this.loginDoador = loginDoador;
    }

    public Long getIdOng() {
        return idOng;
    }
    public void setIdOng(Long idOng) {
        this.idOng = idOng;
    }

    public String getNomeOng() {
        return nomeOng;
    }
    public void setNomeOng(String nomeOng) {
        this.nomeOng = nomeOng;
    }

    public LocalDateTime getData() {
        return data;
    }
    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivoRecusa() {
        return motivoRecusa;
    }
    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }
}
