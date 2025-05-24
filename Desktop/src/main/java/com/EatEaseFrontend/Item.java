package com.EatEaseFrontend;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    /** String bruta vinda do back-end, útil para debug. */
    private transient String ingredientesJson;

    private boolean eComposto;

    private int stockAtual;

    /** Lista já desserializada de ingredientes. */
    private List<ItemIngrediente> ingredientes = new ArrayList<>();

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
        this.ingredientesJson = raw;
        if (raw == null || raw.isBlank())
            return;

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.ingredientes = mapper.readValue(
                    raw,
                    new TypeReference<List<ItemIngrediente>>() {
                    });
        } catch (Exception ex) {
            System.err.println("Falha a ler ingredientes do item id " + id + ": " + ex.getMessage());
        }
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
        return eComposto;
    }

    public void setEComposto(boolean flag) {
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
}
