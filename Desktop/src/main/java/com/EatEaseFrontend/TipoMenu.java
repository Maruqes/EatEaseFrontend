package com.EatEaseFrontend;

/**
 * Model class for TipoMenu data from the API.
 */
public class TipoMenu {
    private int id;
    private String nome;

    public TipoMenu() {
    }

    public TipoMenu(int id, String nome) {
        this.id = id;
        this.nome = nome;
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

    @Override
    public String toString() {
        return "TipoMenu{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
