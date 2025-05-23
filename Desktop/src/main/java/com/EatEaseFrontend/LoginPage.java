package com.EatEaseFrontend;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginPage {
    /**
     * Creates a styled login scene with handlers.
     */
    public static Scene createScene(Stage stage, LoginHandler handler) {
        // Main container - horizontal box with two sides
        HBox root = new HBox();
        root.getStyleClass().add("login-root");

        // Left side - Beautiful gradient panel with floating elements (40% width)
        StackPane leftPanel = new StackPane();
        leftPanel.setPrefWidth(410);
        leftPanel.getStyleClass().add("left-panel");

        // Create a gradient background with depth using orange 600 as base
        Stop[] stops = new Stop[] {
                new Stop(0, Color.web("#E65100")), // Deep orange 900
                new Stop(0.5, Color.web("#FB8C00")), // Orange 600
                new Stop(1, Color.web("#FFB74D")) // Orange 300
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        leftPanel.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        // Add subtle pattern overlay
        Region overlay = new Region();
        overlay.setPrefSize(410, 800);
        overlay.getStyleClass().add("pattern-overlay");

        // Create company logo/branding with more modern styling
        VBox brandingBox = new VBox(25);
        brandingBox.setAlignment(Pos.CENTER);
        brandingBox.getStyleClass().add("branding-container");

        // Restaurant icon with enhanced styling
        ImageView logoView = new ImageView(new Image(LoginPage.class.getResourceAsStream("/images/restaurant.png")));
        logoView.setFitWidth(160);
        logoView.setFitHeight(160);
        logoView.getStyleClass().add("logo-image");

        // Add glow effect to logo
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ffffff", 0.7));
        glow.setRadius(20);
        logoView.setEffect(glow);

        // App name with modern styling
        Text appName = new Text("EatEase");
        appName.setFont(Font.font("System", FontWeight.BOLD, 52));
        appName.setFill(Color.WHITE);
        appName.getStyleClass().add("app-name");

        // Add stylish tagline
        Label tagline = new Label("Gestão de Restaurantes Simplificada");
        tagline.getStyleClass().add("tagline");

        // Add animation for branding elements
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), brandingBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Add all branding elements
        brandingBox.getChildren().addAll(logoView, appName, tagline);
        leftPanel.getChildren().addAll(overlay, brandingBox);

        // Right side - Modern floating card login form (60% width)
        StackPane rightPanelWrapper = new StackPane();
        rightPanelWrapper.getStyleClass().add("right-panel-wrapper");

        VBox rightPanel = new VBox(30);
        rightPanel.setPadding(new Insets(40, 50, 40, 50));
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.getStyleClass().add("right-panel");

        // Add card effect to right panel
        rightPanel.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.15)));
        rightPanel.setMaxWidth(450);

        // Welcome text
        Label welcomeText = new Label("Bem-vindo");
        welcomeText.getStyleClass().add("welcome-text");

        // Login form title with modern styling
        Label loginTitle = new Label("Entre na sua conta");
        loginTitle.getStyleClass().add("login-title");

        // Username field with modern styling
        VBox usernameBox = new VBox(10);
        Label usernameLabel = new Label("Nome de Usuário");
        usernameLabel.getStyleClass().add("input-label");

        TextField usernameFld = new TextField();
        usernameFld.setPromptText("Digite seu nome de usuário");
        usernameFld.getStyleClass().add("input-field");
        usernameFld.setPrefHeight(45);

        usernameBox.getChildren().addAll(usernameLabel, usernameFld);

        // Password field with modern styling
        VBox passwordBox = new VBox(10);
        Label passwordLabel = new Label("Senha");
        passwordLabel.getStyleClass().add("input-label");

        PasswordField passwordFld = new PasswordField();
        passwordFld.setPromptText("Digite sua senha");
        passwordFld.getStyleClass().add("input-field");
        passwordFld.setPrefHeight(45);

        passwordBox.getChildren().addAll(passwordLabel, passwordFld);

        // Remember me checkbox and forgot password (visual only)
        HBox optionsBox = new HBox();
        optionsBox.setAlignment(Pos.CENTER_RIGHT);
        Label forgotPassword = new Label("Esqueceu a senha?");
        forgotPassword.getStyleClass().add("forgot-password");
        optionsBox.getChildren().add(forgotPassword);

        // Modern styled login button
        Button loginBtn = new Button("ENTRAR");
        loginBtn.getStyleClass().add("login-button");
        loginBtn.setPrefHeight(50);
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handler.handle(usernameFld.getText(), passwordFld.getText()));

        // Add some spacing before button
        VBox formBox = new VBox(20);
        formBox.getChildren().addAll(welcomeText, loginTitle, usernameBox, passwordBox, optionsBox, loginBtn);
        formBox.setAlignment(Pos.CENTER);
        formBox.setMaxWidth(350);

        // Add the form to the right panel
        rightPanel.getChildren().add(formBox);
        rightPanelWrapper.getChildren().add(rightPanel);
        HBox.setHgrow(rightPanelWrapper, Priority.ALWAYS);

        // Add both panels to the root container
        root.getChildren().addAll(leftPanel, rightPanelWrapper);

        // Create the scene with modern styling
        Scene scene = new Scene(root, 1024, 700);
        scene.getStylesheets().add(LoginPage.class.getResource("/css/modern_style.css").toExternalForm());

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