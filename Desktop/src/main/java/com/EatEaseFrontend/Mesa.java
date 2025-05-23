package com.EatEaseFrontend;

/**
 * Modelo para representar uma mesa do restaurante.
 */
public class Mesa {
    private int id;
    private int numero;
    private boolean estadoLivre;

    // Default constructor
    public Mesa() {
    }

    // Constructor with fields
    public Mesa(int id, int numero, boolean estadoLivre) {
        this.id = id;
        this.numero = numero;
        this.estadoLivre = estadoLivre;
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

    @Override
    public String toString() {
        return "Mesa{" +
                "id=" + id +
                ", numero=" + numero +
                ", estadoLivre=" + estadoLivre +
                '}';
    }
}
