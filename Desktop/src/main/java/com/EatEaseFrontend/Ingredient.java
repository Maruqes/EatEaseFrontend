package com.EatEaseFrontend;

/**
 * Modelo para representar um ingrediente
 */
public class Ingredient {
    private int id;
    private String nome;
    private int stock;
    private int stock_min;
    private int unidade_id;

    // Default constructor
    public Ingredient() {
    }

    // Constructor for creating new ingredients
    public Ingredient(String nome, int stock, int stock_min, int unidade_id) {
        this.nome = nome;
        this.stock = stock;
        this.stock_min = stock_min;
        this.unidade_id = unidade_id;
    }

    // Getters e setters
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock_min() {
        return stock_min;
    }

    public void setStock_min(int stock_min) {
        this.stock_min = stock_min;
    }

    public int getUnidade_id() {
        return unidade_id;
    }

    public void setUnidade_id(int unidade_id) {
        this.unidade_id = unidade_id;
    }

    /**
     * Retorna o nome da unidade com base no ID
     */
    public String getUnidadeName() {
        switch (unidade_id) {
            case 1:
                return "Unidade";
            case 2:
                return "Grama";
            case 3:
                return "Mililitro";
            case 4:
                return "Quilograma";
            case 5:
                return "Litro";
            default:
                return "Desconhecida";
        }
    }
}