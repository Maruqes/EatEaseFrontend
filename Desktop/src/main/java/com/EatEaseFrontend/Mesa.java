package com.EatEaseFrontend;

/**
 * Modelo para representar uma mesa do restaurante.
 */
public class Mesa {
    private int id;
    private int numero;
    private boolean estadoLivre;
    private int capacidade;

    // Default constructor
    public Mesa() {
    }

    // Constructor with fields
    public Mesa(int id, int numero, boolean estadoLivre, int capacidade) {
        this.id = id;
        this.numero = numero;
        this.estadoLivre = estadoLivre;
        this.capacidade = capacidade;
    }

    // Getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public boolean isEstadoLivre() {
        return estadoLivre;
    }

    public void setEstadoLivre(boolean estadoLivre) {
        this.estadoLivre = estadoLivre;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    @Override
    public String toString() {
        return "Mesa{" +
                "id=" + id +
                ", numero=" + numero +
                ", estadoLivre=" + estadoLivre +
                ", capacidade=" + capacidade +
                '}';
    }
}
