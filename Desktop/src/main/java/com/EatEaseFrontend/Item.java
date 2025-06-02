package com.EatEaseFrontend;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/** Representa um item do menu e faz o parsing automático dos ingredientes. */
@JsonIgnoreProperties(ignoreUnknown = true) // ignora qualquer campo inesperado
public class Item {

    private int id;
    private String nome;

    @JsonProperty("tipoPrato_id")
    private int tipoPratoId;

    private double preco;

    @JsonProperty("eComposto")
    private boolean eComposto;

    private int stockAtual;

    /** Lista já desserializada de ingredientes. */
    private List<ItemIngrediente> ingredientes = new ArrayList<>();

    private String foto;

    /* ---------- Classe interna ---------- */
    public static class ItemIngrediente {
        @JsonProperty("ingredienteId")
        private int ingredienteId;
        private int quantidade;

        public int getIngredienteId() {
            return ingredienteId;
        }

        public void setIngredienteId(int ingredienteId) {
            this.ingredienteId = ingredienteId;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(int quantidade) {
            this.quantidade = quantidade;
        }

        @Override
        public String toString() {
            return "ItemIngrediente{id=%d, qtd=%d}".formatted(ingredienteId, quantidade);
        }
    }

    /* ---------- Deserialização dos ingredientes ---------- */
    @JsonProperty("ingredientesJson")
    private void unpackIngredientes(String raw) {
        if (raw == null || raw.isBlank())
            return;

        try {
            // Debug: print the raw JSON being parsed
            System.out.println("[DEBUG] Parsing ingredientesJson for " + nome + ": " + raw);

            // Handle escaped JSON strings properly
            String cleanedJson = raw;

            // If the string contains escaped quotes, unescape them
            if (raw.contains("\\\"")) {
                cleanedJson = raw.replace("\\\"", "\"")
                        .replace("\\\\", "\\");
                System.out.println("[DEBUG] Cleaned JSON: " + cleanedJson);
            }

            ObjectMapper mapper = new ObjectMapper();
            this.ingredientes = mapper.readValue(
                    cleanedJson,
                    new TypeReference<List<ItemIngrediente>>() {
                    });

            System.out.println("[DEBUG] Successfully parsed " + ingredientes.size() + " ingredientes for " + nome);
        } catch (Exception ex) {
            System.err.println("Falha a ler ingredientes do item id " + id + " (" + nome + "): " + ex.getMessage());
            System.err.println("Raw JSON: " + raw);
            // Initialize empty list to avoid null pointer exceptions
            this.ingredientes = new ArrayList<>();
        }
    }

    /* ---------- Constructors ---------- */
    public Item() {
        // Default constructor needed for Jackson deserialization
    }

    public Item(int id, String nome, double preco, int tipoPratoId) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.tipoPratoId = tipoPratoId;
    }

    /* ---------- Getters / setters ---------- */
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getTipoPratoId() {
        return tipoPratoId;
    }

    public double getPreco() {
        return preco;
    }

    public boolean isEComposto() {
        System.out.println("[DEBUG] Getting eComposto for " + nome + ": " + eComposto);
        return eComposto;
    }

    public void setEComposto(boolean flag) {
        System.out.println("[DEBUG] Setting eComposto for " + nome + " to: " + flag);
        this.eComposto = flag;
    } // <-- OBRIGATÓRIO

    public int getStockAtual() {
        return stockAtual;
    }

    public void setStockAtual(int stockAtual) {
        this.stockAtual = stockAtual;
    }

    public List<ItemIngrediente> getIngredientes() {
        return ingredientes;
    }

    public String getTipoPratoName() {
        return switch (tipoPratoId) {
            case 1 -> "Entrada";
            case 2 -> "Prato Principal";
            case 3 -> "Sobremesa";
            case 4 -> "Bebida";
            default -> "Desconhecido";
        };
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setTipoPratoId(int tipoPratoId) {
        this.tipoPratoId = tipoPratoId;
    }

    public void setStock(int stock) {
        this.stockAtual = stock;
    }
}
