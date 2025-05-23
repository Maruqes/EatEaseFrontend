package com.EatEaseFrontend;

import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.control.Dialog;

/**
 * Classe auxiliar para gerenciar referências globais ao Stage principal
 * e fornecer métodos utilitários para diálogos.
 */
public class StageManager {

    // Referência estática à janela principal da aplicação
    private static Stage primaryStage;

    /**
     * Configura a referência à janela principal
     * 
     * @param stage A instância do Stage principal
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Obtém a referência ao stage principal
     * 
     * @return A instância do Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Configura um Dialog para usar o primaryStage como owner e evitar resize
     * 
     * @param dialog Diálogo a ser configurado
     */
    public static void setupDialog(Dialog<?> dialog) {
        if (dialog != null) {
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.APPLICATION_MODAL);

            // Impedir redimensionamento
            Node dialogPane = dialog.getDialogPane();
            if (dialogPane != null) {
                Stage stage = (Stage) dialogPane.getScene().getWindow();
                if (stage != null) {
                    stage.setResizable(false);
                }
            }
        }
    }

    /**
     * Configura um estágio para usar o primaryStage como owner e evitar resize
     * 
     * @param stage Estágio a ser configurado
     */
    public static void setupStage(Stage stage) {
        if (stage != null) {
            stage.initOwner(primaryStage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
        }
    }
}
