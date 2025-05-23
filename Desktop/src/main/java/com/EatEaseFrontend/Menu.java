package com.EatEaseFrontend;

/**
 * Model class for Menu data from the API.
 */
public class Menu {
    private int id;
    private String nome;
    private String descricao;
    private int tipoMenuId;

    public Menu() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getTipoMenuId() {
        return tipoMenuId;
    }

    public void setTipoMenuId(int tipoMenuId) {
        this.tipoMenuId = tipoMenuId;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", tipoMenuId=" + tipoMenuId +
                '}';
    }
}
