package com.EatEaseFrontend;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de estado de loading para componentes da UI
 * Previne múltiplas operações simultâneas e fornece feedback visual
 */
public class LoadingStateManager {
    
    private final Set<String> loadingOperations = ConcurrentHashMap.newKeySet();
    private final Set<Button> disabledButtons = ConcurrentHashMap.newKeySet();
    
    /**
     * Inicia um estado de loading para uma operação específica
     * 
     * @param operationId ID único da operação
     * @param buttons Botões a serem desabilitados durante a operação
     * @return true se o loading foi iniciado, false se já estava em execução
     */
    public boolean startLoading(String operationId, Button... buttons) {
        if (loadingOperations.contains(operationId)) {
            return false; // Operação já em execução
        }
        
        loadingOperations.add(operationId);
        
        Platform.runLater(() -> {
            for (Button button : buttons) {
                button.setDisable(true);
                disabledButtons.add(button);
            }
        });
        
        return true;
    }
    
    /**
     * Finaliza um estado de loading para uma operação específica
     * 
     * @param operationId ID único da operação
     */
    public void stopLoading(String operationId) {
        loadingOperations.remove(operationId);
        
        // Se não há mais operações em execução, reabilitar botões
        if (loadingOperations.isEmpty()) {
            Platform.runLater(() -> {
                for (Button button : disabledButtons) {
                    button.setDisable(false);
                }
                disabledButtons.clear();
            });
        }
    }
    
    /**
     * Verifica se uma operação específica está em execução
     * 
     * @param operationId ID da operação
     * @return true se a operação está em execução
     */
    public boolean isLoading(String operationId) {
        return loadingOperations.contains(operationId);
    }
    
    /**
     * Verifica se há qualquer operação em execução
     * 
     * @return true se há operações em execução
     */
    public boolean hasAnyLoading() {
        return !loadingOperations.isEmpty();
    }
    
    /**
     * Para todas as operações de loading
     */
    public void stopAllLoading() {
        loadingOperations.clear();
        Platform.runLater(() -> {
            for (Button button : disabledButtons) {
                button.setDisable(false);
            }
            disabledButtons.clear();
        });
    }
    
    /**
     * Cria um ProgressIndicator com configurações padrão
     * 
     * @return ProgressIndicator configurado
     */
    public static ProgressIndicator createProgressIndicator() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(30, 30);
        return progress;
    }
}
