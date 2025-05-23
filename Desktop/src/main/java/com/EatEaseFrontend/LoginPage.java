package com.EatEaseFrontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginPage {
    /**
     * Creates a styled login scene with handlers.
     */
    public static Scene createScene(Stage stage, LoginHandler handler) {
        // Main container - horizontal box with two sides
        HBox root = new HBox();

        // Left side - Image panel (40% width)
        StackPane leftPanel = new StackPane();
        leftPanel.setPrefWidth(410); // 40% of 1024
        leftPanel.getStyleClass().add("left-panel");

        // Create company logo/branding
        VBox brandingBox = new VBox(20);
        brandingBox.setAlignment(Pos.CENTER);

        // Restaurant icon
        ImageView logoView = new ImageView(new Image(LoginPage.class.getResourceAsStream("/images/restaurant.png")));
        logoView.setFitWidth(200);
        logoView.setFitHeight(200);
        logoView.getStyleClass().add("logo-image");

        // App name with shadow effect
        Text appName = new Text("EatEase");
        appName.setFont(Font.font("System", FontWeight.BOLD, 48));
        appName.setFill(Color.WHITE);
        appName.getStyleClass().add("app-name");

        // Tagline
        Label tagline = new Label("Gestão de Restaurantes Simplificada");
        tagline.getStyleClass().add("tagline");

        // Add all branding elements
        brandingBox.getChildren().addAll(logoView, appName, tagline);
        leftPanel.getChildren().add(brandingBox);

        // Right side - Login form (60% width)
        VBox rightPanel = new VBox(25);
        rightPanel.setPadding(new Insets(50, 60, 50, 60));
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.getStyleClass().add("right-panel");

        // Login form title
        Label loginTitle = new Label("Login");
        loginTitle.getStyleClass().add("login-title");

        // Username field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Nome de Usuário");
        usernameLabel.getStyleClass().add("input-label");

        TextField usernameFld = new TextField();
        usernameFld.setPromptText("Digite seu nome de usuário");
        usernameFld.getStyleClass().add("input");

        usernameBox.getChildren().addAll(usernameLabel, usernameFld);

        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Senha");
        passwordLabel.getStyleClass().add("input-label");

        PasswordField passwordFld = new PasswordField();
        passwordFld.setPromptText("Digite sua senha");
        passwordFld.getStyleClass().add("input");

        passwordBox.getChildren().addAll(passwordLabel, passwordFld);

        // Login button
        Button loginBtn = new Button("Entrar");
        loginBtn.getStyleClass().add("login-button");
        loginBtn.setOnAction(e -> handler.handle(usernameFld.getText(), passwordFld.getText()));

        // Add some spacing before button
        VBox formBox = new VBox(25);
        formBox.getChildren().addAll(loginTitle, usernameBox, passwordBox, loginBtn);
        formBox.setAlignment(Pos.CENTER);

        // Add the form to the right panel
        rightPanel.getChildren().add(formBox);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        // Add both panels to the root container
        root.getChildren().addAll(leftPanel, rightPanel);

        // Create the scene
        Scene scene = new Scene(root, 1024, 800);
        scene.getStylesheets().add(LoginPage.class.getResource("/css/css.css").toExternalForm());
        return scene;
    }

    /**
     * Simple functional interface to pass login action.
     */
    @FunctionalInterface
    public interface LoginHandler {
        void handle(String username, String password);
    }
}