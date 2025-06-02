package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.ErrorMessages;
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

    // /**
    // * Shows an improved error popup with better messaging for HTTP errors
    // *
    // * @param title The title of the dialog
    // * @param header The header text of the dialog
    // * @param statusCode The HTTP status code
    // */
    // public static void showHttpErrorPopup(String title, String header, int
    // statusCode) {
    // String improvedMessage = ErrorMessages.formatHttpError(statusCode);
    // showPopupDialog(Alert.AlertType.ERROR, title, header, improvedMessage);
    // }

    /**
     * Shows an improved error popup with better messaging for exceptions
     *
     * @param title     The title of the dialog
     * @param header    The header text of the dialog
     * @param exception The exception message
     */
    public static void showExceptionErrorPopup(String title, String header,
            String exception) {
        String improvedMessage = ErrorMessages.formatExceptionError(exception);
        showPopupDialog(Alert.AlertType.ERROR, title, header, improvedMessage);
    }

    /**
     * Shows a predefined error popup for ingredient loading failures
     */
    public static void showIngredientLoadError() {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Ingredients.LOAD_TITLE,
                ErrorMessages.Ingredients.LOAD_HEADER,
                ErrorMessages.Ingredients.LOAD_MESSAGE);
    }

    /**
     * Shows a predefined error popup for ingredient loading failures with status
     * code
     */
    public static void showIngredientLoadError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Ingredients.LOAD_TITLE,
                ErrorMessages.Ingredients.LOAD_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for ingredient loading failures with
     * exception
     */
    public static void showIngredientLoadError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Ingredients.LOAD_TITLE,
                ErrorMessages.Ingredients.LOAD_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    /**
    * Shows a predefined error popup for item loading failures
    */
    public static void showItemLoadError() {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Items.LOAD_TITLE,
    ErrorMessages.Items.LOAD_HEADER,
    ErrorMessages.Items.LOAD_MESSAGE);
    }

    /**
    * Shows a predefined error popup for item loading failures with status code
    */
    public static void showItemLoadError(int statusCode) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Items.LOAD_TITLE,
    ErrorMessages.Items.LOAD_HEADER,
    ErrorMessages.formatHttpError(statusCode));
    }

    /**
    * Shows a predefined error popup for item loading failures with exception
    */
    public static void showItemLoadError(String exception) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Items.LOAD_TITLE,
    ErrorMessages.Items.LOAD_HEADER,
    ErrorMessages.formatExceptionError(exception));
    }

    /**
     * Shows a predefined error popup for employee loading failures
     */
    public static void showEmployeeLoadError() {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.LOAD_TITLE,
                ErrorMessages.Employees.LOAD_HEADER,
                ErrorMessages.Employees.LOAD_MESSAGE);
    }

    /**
     * Shows a predefined error popup for employee loading failures with status
     * code
     */
    public static void showEmployeeLoadError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.LOAD_TITLE,
                ErrorMessages.Employees.LOAD_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for employee loading failures with exception
     */
    public static void showEmployeeLoadError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.LOAD_TITLE,
                ErrorMessages.Employees.LOAD_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    // /**
    // * Shows a predefined error popup for order loading failures
    // */
    // public static void showOrderLoadError() {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Orders.LOAD_TITLE,
    // ErrorMessages.Orders.LOAD_HEADER,
    // ErrorMessages.Orders.LOAD_MESSAGE);
    // }

    // /**
    // * Shows a predefined error popup for order loading failures with status code
    // */
    // public static void showOrderLoadError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Orders.LOAD_TITLE,
    // ErrorMessages.Orders.LOAD_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for order loading failures with exception
    // */
    // public static void showOrderLoadError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Orders.LOAD_TITLE,
    // ErrorMessages.Orders.LOAD_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    /**
    * Shows a predefined error popup for menu loading failures
    */
    public static void showMenuLoadError() {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Menus.LOAD_TITLE,
    ErrorMessages.Menus.LOAD_HEADER,
    ErrorMessages.Menus.LOAD_MESSAGE);
    }

    /**
    * Shows a predefined error popup for menu loading failures with status code
    */
    public static void showMenuLoadError(int statusCode) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Menus.LOAD_TITLE,
    ErrorMessages.Menus.LOAD_HEADER,
    ErrorMessages.formatHttpError(statusCode));
    }

    /**
    * Shows a predefined error popup for menu loading failures with exception
    */
    public static void showMenuLoadError(String exception) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Menus.LOAD_TITLE,
    ErrorMessages.Menus.LOAD_HEADER,
    ErrorMessages.formatExceptionError(exception));
    }

    // Additional ingredient operation methods
    /**
     * Shows a predefined error popup for ingredient add failures
     */
    public static void showIngredientAddError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Ingredients.ADD_TITLE,
                ErrorMessages.Ingredients.ADD_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for ingredient add failures with exception
     */
    public static void showIngredientAddError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Ingredients.ADD_TITLE,
                ErrorMessages.Ingredients.ADD_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    // /**
    // * Shows a predefined error popup for ingredient update failures
    // */
    // public static void showIngredientUpdateError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.UPDATE_TITLE,
    // ErrorMessages.Ingredients.UPDATE_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for ingredient update failures with
    // exception
    // */
    // public static void showIngredientUpdateError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.UPDATE_TITLE,
    // ErrorMessages.Ingredients.UPDATE_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // /**
    // * Shows a predefined error popup for ingredient delete failures
    // */
    // public static void showIngredientDeleteError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.DELETE_TITLE,
    // ErrorMessages.Ingredients.DELETE_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for ingredient delete failures with
    // exception
    // */
    // public static void showIngredientDeleteError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.DELETE_TITLE,
    // ErrorMessages.Ingredients.DELETE_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // /**
    // * Shows a predefined error popup for ingredient stock movement failures
    // */
    // public static void showIngredientStockError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.STOCK_TITLE,
    // ErrorMessages.Ingredients.STOCK_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for ingredient stock movement failures with
    // * exception
    // */
    // public static void showIngredientStockError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Ingredients.STOCK_TITLE,
    // ErrorMessages.Ingredients.STOCK_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // Additional item operation methods
    /**
    * Shows a predefined error popup for item create failures
    */
    public static void showItemCreateError(int statusCode) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Items.CREATE_TITLE,
    ErrorMessages.Items.CREATE_HEADER,
    ErrorMessages.formatHttpError(statusCode));
    }

    /**
    * Shows a predefined error popup for item create failures with exception
    */
    public static void showItemCreateError(String exception) {
    showPopupDialog(Alert.AlertType.ERROR,
    ErrorMessages.Items.CREATE_TITLE,
    ErrorMessages.Items.CREATE_HEADER,
    ErrorMessages.formatExceptionError(exception));
    }

    // /**
    // * Shows a predefined error popup for item update failures
    // */
    // public static void showItemUpdateError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.UPDATE_TITLE,
    // ErrorMessages.Items.UPDATE_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for item update failures with exception
    // */
    // public static void showItemUpdateError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.UPDATE_TITLE,
    // ErrorMessages.Items.UPDATE_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // /**
    // * Shows a predefined error popup for item delete failures
    // */
    // public static void showItemDeleteError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.DELETE_TITLE,
    // ErrorMessages.Items.DELETE_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for item delete failures with exception
    // */
    // public static void showItemDeleteError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.DELETE_TITLE,
    // ErrorMessages.Items.DELETE_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // /**
    // * Shows a predefined error popup for item ingredient loading failures
    // */
    // public static void showItemIngredientsLoadError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.LOAD_INGREDIENTS_TITLE,
    // ErrorMessages.Items.LOAD_INGREDIENTS_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for item ingredient loading failures with
    // * exception
    // */
    // public static void showItemIngredientsLoadError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Items.LOAD_INGREDIENTS_TITLE,
    // ErrorMessages.Items.LOAD_INGREDIENTS_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    // Additional employee operation methods
    /**
     * Shows a predefined error popup for employee create failures
     */
    public static void showEmployeeCreateError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.CREATE_TITLE,
                ErrorMessages.Employees.CREATE_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for employee create failures with exception
     */
    public static void showEmployeeCreateError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.CREATE_TITLE,
                ErrorMessages.Employees.CREATE_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    /**
     * Shows a predefined error popup for employee update failures
     */
    public static void showEmployeeUpdateError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.UPDATE_TITLE,
                ErrorMessages.Employees.UPDATE_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for employee update failures with exception
     */
    public static void showEmployeeUpdateError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Employees.UPDATE_TITLE,
                ErrorMessages.Employees.UPDATE_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    // /**
    // * Shows a predefined error popup for order status update failures
    // */
    // public static void showOrderStatusUpdateError(int statusCode) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Orders.UPDATE_STATUS_TITLE,
    // ErrorMessages.Orders.UPDATE_STATUS_HEADER,
    // ErrorMessages.formatHttpError(statusCode));
    // }

    // /**
    // * Shows a predefined error popup for order status update failures with
    // * exception
    // */
    // public static void showOrderStatusUpdateError(String exception) {
    // showPopupDialog(Alert.AlertType.ERROR,
    // ErrorMessages.Orders.UPDATE_STATUS_TITLE,
    // ErrorMessages.Orders.UPDATE_STATUS_HEADER,
    // ErrorMessages.formatExceptionError(exception));
    // }

    /**
     * Shows a predefined error popup for menu delete failures
     */
    public static void showMenuDeleteError(int statusCode) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Menus.DELETE_TITLE,
                ErrorMessages.Menus.DELETE_HEADER,
                ErrorMessages.formatHttpError(statusCode));
    }

    /**
     * Shows a predefined error popup for menu delete failures with exception
     */
    public static void showMenuDeleteError(String exception) {
        showPopupDialog(Alert.AlertType.ERROR,
                ErrorMessages.Menus.DELETE_TITLE,
                ErrorMessages.Menus.DELETE_HEADER,
                ErrorMessages.formatExceptionError(exception));
    }

    // // Success popup methods using predefined success messages

    // /**
    // * Shows a predefined success popup for ingredient add operations
    // */
    // public static void showIngredientAddSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Ingredients.ADD_TITLE,
    // ErrorMessages.SuccessMessages.Ingredients.ADD_HEADER,
    // ErrorMessages.SuccessMessages.Ingredients.ADD_MESSAGE);
    // }

    // /**
    // * Shows a predefined success popup for ingredient update operations
    // */
    // public static void showIngredientUpdateSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Ingredients.UPDATE_TITLE,
    // ErrorMessages.SuccessMessages.Ingredients.UPDATE_HEADER,
    // ErrorMessages.SuccessMessages.Ingredients.UPDATE_MESSAGE);
    // }

    // /**
    // * Shows a predefined success popup for ingredient delete operations
    // */
    // public static void showIngredientDeleteSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Ingredients.DELETE_TITLE,
    // ErrorMessages.SuccessMessages.Ingredients.DELETE_HEADER,
    // ErrorMessages.SuccessMessages.Ingredients.DELETE_MESSAGE);
    // }

    // /**
    // * Shows a predefined success popup for ingredient stock operations
    // */
    // public static void showIngredientStockSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Ingredients.STOCK_TITLE,
    // ErrorMessages.SuccessMessages.Ingredients.STOCK_HEADER,
    // ErrorMessages.SuccessMessages.Ingredients.STOCK_MESSAGE);
    // }

    /**
    * Shows a predefined success popup for item create operations
    */
    public static void showItemCreateSuccess() {
    showPopupDialog(Alert.AlertType.INFORMATION,
    ErrorMessages.SuccessMessages.Items.CREATE_TITLE,
    ErrorMessages.SuccessMessages.Items.CREATE_HEADER,
    ErrorMessages.SuccessMessages.Items.CREATE_MESSAGE);
    }

    // /**
    // * Shows a predefined success popup for item update operations
    // */
    // public static void showItemUpdateSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Items.UPDATE_TITLE,
    // ErrorMessages.SuccessMessages.Items.UPDATE_HEADER,
    // ErrorMessages.SuccessMessages.Items.UPDATE_MESSAGE);
    // }

    // /**
    // * Shows a predefined success popup for item delete operations
    // */
    // public static void showItemDeleteSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Items.DELETE_TITLE,
    // ErrorMessages.SuccessMessages.Items.DELETE_HEADER,
    // ErrorMessages.SuccessMessages.Items.DELETE_MESSAGE);
    // }

    /**
     * Shows a predefined success popup for employee create operations
     */
    public static void showEmployeeCreateSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Employees.CREATE_TITLE,
                ErrorMessages.SuccessMessages.Employees.CREATE_HEADER,
                ErrorMessages.SuccessMessages.Employees.CREATE_MESSAGE);
    }

    /**
     * Shows a predefined success popup for employee update operations
     */
    public static void showEmployeeUpdateSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Employees.UPDATE_TITLE,
                ErrorMessages.SuccessMessages.Employees.UPDATE_HEADER,
                ErrorMessages.SuccessMessages.Employees.UPDATE_MESSAGE);
    }

    // /**
    // * Shows a predefined success popup for order status update operations
    // */
    // public static void showOrderStatusUpdateSuccess() {
    // showPopupDialog(Alert.AlertType.INFORMATION,
    // ErrorMessages.SuccessMessages.Orders.UPDATE_STATUS_TITLE,
    // ErrorMessages.SuccessMessages.Orders.UPDATE_STATUS_HEADER,
    // ErrorMessages.SuccessMessages.Orders.UPDATE_STATUS_MESSAGE);
    // }

    /**
     * Shows a predefined success popup for menu create operations
     */
    public static void showMenuCreateSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Menus.CREATE_TITLE,
                ErrorMessages.SuccessMessages.Menus.CREATE_HEADER,
                ErrorMessages.SuccessMessages.Menus.CREATE_MESSAGE);
    }

    /**
    * Shows a predefined success popup for menu update operations
    */
    public static void showMenuUpdateSuccess() {
    showPopupDialog(Alert.AlertType.INFORMATION,
    ErrorMessages.SuccessMessages.Menus.UPDATE_TITLE,
    ErrorMessages.SuccessMessages.Menus.UPDATE_HEADER,
    ErrorMessages.SuccessMessages.Menus.UPDATE_MESSAGE);
    }

    /**
     * Shows a predefined success popup for menu delete operations
     */
    public static void showMenuDeleteSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Menus.DELETE_TITLE,
                ErrorMessages.SuccessMessages.Menus.DELETE_HEADER,
                ErrorMessages.SuccessMessages.Menus.DELETE_MESSAGE);
    }

    /**
     * Shows a predefined success popup for table create operations
     */
    public static void showTableCreateSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Tables.CREATE_TITLE,
                ErrorMessages.SuccessMessages.Tables.CREATE_HEADER,
                ErrorMessages.SuccessMessages.Tables.CREATE_MESSAGE);
    }

    /**
     * Shows a predefined success popup for table free operations
     */
    public static void showTableFreeSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Tables.STATUS_UPDATE_TITLE,
                ErrorMessages.SuccessMessages.Tables.STATUS_UPDATE_HEADER,
                ErrorMessages.SuccessMessages.Tables.FREE_MESSAGE);
    }

    /**
     * Shows a predefined success popup for table occupied operations
     */
    public static void showTableOccupiedSuccess() {
        showPopupDialog(Alert.AlertType.INFORMATION,
                ErrorMessages.SuccessMessages.Tables.STATUS_UPDATE_TITLE,
                ErrorMessages.SuccessMessages.Tables.STATUS_UPDATE_HEADER,
                ErrorMessages.SuccessMessages.Tables.OCCUPIED_MESSAGE);
    }
}
