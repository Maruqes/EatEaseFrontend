package com.EatEaseFrontend;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    /**
     * Shows an error alert dialog with header and message
     * 
     * @param title   The dialog title
     * @param header  The dialog header text
     * @param message The dialog content message
     */
    public static void showErrorAlert(String title, String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);
            configureDialog(alert);
            alert.showAndWait();
        });
    }

    /**
     * Shows an error alert dialog with just a title and message
     * 
     * @param title   The dialog title
     * @param message The dialog content message
     */
    public static void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            configureDialog(alert);
            alert.showAndWait();
        });
    }

    /**
     * Shows an information alert dialog
     * 
     * @param title   The dialog title
     * @param message The dialog content message
     */
    public static void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            configureDialog(alert);
            alert.showAndWait();
        });
    }

    /**
     * Shows a confirmation dialog and returns true if the user clicked OK
     * 
     * @param title   The dialog title
     * @param message The dialog content message
     * @return true if the user confirms, false otherwise
     */
    public static boolean showConfirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        configureDialog(alert);

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

}