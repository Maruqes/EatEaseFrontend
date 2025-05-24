package com.EatEaseFrontend;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Gerenciador centralizado para operações assíncronas
 * Previne race conditions e garante sincronização adequada
 */
public class AsyncOperationManager {
    
    private final AtomicBoolean isOperationRunning = new AtomicBoolean(false);
    
    /**
     * Executa uma operação assíncrona com proteção contra execução simultânea
     * 
     * @param operation Operação a ser executada em thread separada
     * @param onSuccess Callback executado na UI thread em caso de sucesso
     * @param onError Callback executado na UI thread em caso de erro
     * @param <T> Tipo do resultado da operação
     * @return CompletableFuture da operação
     */
    public <T> CompletableFuture<T> executeOperation(
            Supplier<T> operation,
            Runnable onSuccess,
            Runnable onError) {
        
        // Verificar se já há uma operação em execução
        if (!isOperationRunning.compareAndSet(false, true)) {
            // Operação já em execução, retornar future falhado
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("Operação já em execução"));
            return failedFuture;
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.get();
            } finally {
                isOperationRunning.set(false);
            }
        }).whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    if (onError != null) {
                        onError.run();
                    }
                } else {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
            });
        });
    }
    
    /**
     * Executa uma operação assíncrona simples sem proteção de concorrência
     * 
     * @param operation Operação a ser executada
     * @param onComplete Callback executado após completion
     * @param <T> Tipo do resultado
     * @return CompletableFuture da operação
     */
    public <T> CompletableFuture<T> executeSimpleOperation(
            Supplier<T> operation,
            Runnable onComplete) {
        
        return CompletableFuture.supplyAsync(operation)
                .whenComplete((result, throwable) -> {
                    if (onComplete != null) {
                        Platform.runLater(onComplete);
                    }
                });
    }
    
    /**
     * Verifica se há uma operação em execução
     * 
     * @return true se há operação em execução
     */
    public boolean isOperationRunning() {
        return isOperationRunning.get();
    }
    
    /**
     * Força o reset do estado de operação (usar com cuidado)
     */
    public void forceReset() {
        isOperationRunning.set(false);
    }
}
