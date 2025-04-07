package com.EatEaseFrontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");

        Label titulo = new Label("Bem-vindo ao EatEase");
        titulo.getStyleClass().add("titulo");

        TextField email = new TextField();
        email.setPromptText("Email");
        email.getStyleClass().add("input");

        PasswordField password = new PasswordField();
        password.setPromptText("Palavra-passe");
        password.getStyleClass().add("input");

        Button loginBtn = new Button("Entrar");
        loginBtn.getStyleClass().add("botao");
        loginBtn.setEffect(new DropShadow(5, Color.GRAY));

        layout.getChildren().addAll(titulo, email, password, loginBtn);

        Scene cena = new Scene(layout, 400, 350);
        cena.getStylesheets().add(getClass().getResource("/css/css.css").toExternalForm());

        stage.setTitle("Login Moderno");
        stage.setScene(cena);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
