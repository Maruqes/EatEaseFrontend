package com.EatEaseFrontend;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Classe utilitária para configurar diálogos e janelas
 * de forma que eles usem o primaryStage como owner e não possam ser
 * redimensionados.
 */
public class DialogHelper {

    /**
     * Configura um diálogo para usar o primaryStage como owner e não ser
     * redimensionável
     * 
     * @param dialog O diálogo a ser configurado
     */
    public static void configureDialog(Dialog<?> dialog) {
        // Obter o primaryStage do StageManager
        Stage primaryStage = StageManager.getPrimaryStage();

        if (dialog != null && primaryStage != null) {
            // Configurar o owner do diálogo
            dialog.initOwner(primaryStage);
            dialog.initModality(Modality.APPLICATION_MODAL);

            // Após o diálogo ser inicializado, podemos acessar sua janela
            dialog.setOnShown(e -> {
                Node dialogPane = dialog.getDialogPane();
                if (dialogPane != null && dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                    Stage stage = (Stage) dialogPane.getScene().getWindow();
                    stage.setResizable(false);
                }
            });
        }
    }

    /**
     * Configura um Stage para usar o primaryStage como owner e não ser
     * redimensionável
     * 
     * @param stage O stage a ser configurado
     */
    public static void configureStage(Stage stage) {
        // Obter o primaryStage do StageManager
        Stage primaryStage = StageManager.getPrimaryStage();

        if (stage != null && primaryStage != null) {
            // Configurar o owner do stage
            stage.initOwner(primaryStage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
        }
    }

}