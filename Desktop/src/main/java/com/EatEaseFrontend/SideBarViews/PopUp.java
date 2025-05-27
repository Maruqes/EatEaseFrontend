package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.StageManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class PopUp {
    /**
     * Creates and shows a popup dialog with the specified type, title, header, and
     * content
     * 
     * @param type    The type of dialog (ERROR, INFORMATION, WARNING, etc.)
     * @param title   The title of the dialog
     * @param header  The header text of the dialog
     * @param content The content text of the dialog
     */
    public static void showPopupDialog(Alert.AlertType type, String title, String header, String content) {
        Stage primaryStage = StageManager.getPrimaryStage();
        double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        // Create the popup content
        VBox popupContent = new VBox(15);
        popupContent.getStyleClass().add("popup-container");
        popupContent.setPrefWidth(350);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("popup-title");

        // Set title color based on type
        Color titleColor = Color.BLACK;
        if (type == Alert.AlertType.ERROR) {
            titleColor = Color.RED;
        } else if (type == Alert.AlertType.WARNING) {
            titleColor = Color.ORANGE;
        } else if (type == Alert.AlertType.INFORMATION) {
            titleColor = Color.BLUE;
        }
        titleLabel.setTextFill(titleColor);

        // Header
        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        headerLabel.setWrapText(true);

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // OK Button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("login-button");
        okButton.setOnAction(e -> popup.hide());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(okButton);

        popupContent.getChildren().addAll(titleLabel, headerLabel, contentLabel, buttonBox);
        popup.getContent().add(popupContent);

        // Show popup centered
        popup.show(primaryStage, centerX - 175, centerY - 100);
    }

    /**
     * Creates and shows a confirmation popup dialog with Yes/No buttons
     * 
     * @param type      The type of dialog (ERROR, INFORMATION, WARNING, etc.)
     * @param title     The title of the dialog
     * @param header    The header text of the dialog
     * @param content   The content text of the dialog
     * @param onConfirm Callback to execute when user confirms (clicks Yes)
     */
    public static void showConfirmationPopup(Alert.AlertType type, String title, String header, String content,
            Runnable onConfirm) {
        Stage primaryStage = StageManager.getPrimaryStage();
        double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        // Create the popup content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        popupContent.setPrefWidth(400);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Set title color based on type
        Color titleColor = Color.BLACK;
        if (type == Alert.AlertType.ERROR) {
            titleColor = Color.RED;
        } else if (type == Alert.AlertType.WARNING) {
            titleColor = Color.ORANGE;
        } else if (type == Alert.AlertType.INFORMATION) {
            titleColor = Color.BLUE;
        }
        titleLabel.setTextFill(titleColor);

        // Header
        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        headerLabel.setWrapText(true);

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button noButton = new Button("Não");
        noButton.getStyleClass().add("login-button");
        noButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        noButton.setOnAction(e -> popup.hide());

        Button yesButton = new Button("Sim");
        yesButton.getStyleClass().add("login-button");
        yesButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        yesButton.setOnAction(e -> {
            popup.hide();
            if (onConfirm != null) {
                onConfirm.run();
            }
        });

        buttonBox.getChildren().addAll(yesButton, noButton);

        popupContent.getChildren().addAll(titleLabel, headerLabel, contentLabel, buttonBox);
        popup.getContent().add(popupContent);

        // Show popup centered
        popup.show(primaryStage, centerX - 200, centerY - 100);
    }
}
