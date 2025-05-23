package com.EatEaseFrontend;

import java.util.List;

/**
 * Classe que representa um pedido do restaurante
 */
public class Pedido {
    private int id;
    private List<Integer> itensIds;
    private int estadoPedido_id;
    private int mesa_id;
    private int funcionario_id;
    private String dataHora;
    private String observacao;
    private List<Integer> ingredientesRemover;

    // Getters e setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getItensIds() {
        return itensIds;
    }

    public void setItensIds(List<Integer> itensIds) {
        this.itensIds = itensIds;
    }

    public int getEstadoPedido_id() {
        return estadoPedido_id;
    }

    public void setEstadoPedido_id(int estadoPedido_id) {
        this.estadoPedido_id = estadoPedido_id;
    }

    public int getMesa_id() {
        return mesa_id;
    }

    public void setMesa_id(int mesa_id) {
        this.mesa_id = mesa_id;
    }

    public int getFuncionario_id() {
        return funcionario_id;
    }

    public void setFuncionario_id(int funcionario_id) {
        this.funcionario_id = funcionario_id;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Integer> getIngredientesRemover() {
        return ingredientesRemover;
    }

    public void setIngredientesRemover(List<Integer> ingredientesRemover) {
        this.ingredientesRemover = ingredientesRemover;
    }

    /**
     * Retorna o nome do estado do pedido com base no ID
     * 
     * @return String representando o estado do pedido
     */
    public String getEstadoNome() {
        switch (estadoPedido_id) {
            case 1:
                return "Em preparo";
            case 2:
                return "Pronto";
            case 3:
                return "Entregue";
            case 4:
                return "Cancelado";
            case 5:
                return "Pendente";
            default:
                return "Estado desconhecido";
        }
    }

    /**
     * Formata a data e hora para exibição
     * 
     * @return Data e hora formatadas
     */
    public String getDataHoraFormatada() {
        if (dataHora == null || dataHora.isEmpty()) {
            return "Data não disponível";
        }

        try {
            // Extraindo a data e hora principal sem a parte do fuso horário
            String[] parts = dataHora.split("T");
            String date = parts[0]; // YYYY-MM-DD
            String time = parts[1].split("\\.")[0]; // HH:MM:SS

            // Formatando para o padrão brasileiro
            String[] dateParts = date.split("-");
            return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] + " " + time;
        } catch (Exception e) {
            return dataHora; // Se houver erro, retorna a string original
        }
    }
}
