package com.EatEaseFrontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main extends Application {

    // cookies + http partilhados
    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient http = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    // guardo a Stage para trocar de cena depois do login
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Label titulo = new Label("Bem-vindo ao EatEase");

        TextField usernameFld = new TextField();
        usernameFld.setPromptText("Username");

        PasswordField passwordFld = new PasswordField();
        passwordFld.setPromptText("Password");

        Button loginBtn = new Button("Entrar");
        loginBtn.setEffect(new DropShadow(5, Color.GRAY));

        // usa o texto dos inputs
        loginBtn.setOnAction(e -> fazerLoginEListarFuncionarios(usernameFld.getText(), passwordFld.getText()));

        layout.getChildren().addAll(titulo, usernameFld, passwordFld, loginBtn);

        Scene cena = new Scene(layout, 400, 350);
        stage.setTitle("Login");
        stage.setScene(cena);
        stage.show();
    }

    private void fazerLoginEListarFuncionarios(String username, String password) {
        String form = "username=" + username + "&password=" + password;

        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        http.sendAsync(loginReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    cookieManager.getCookieStore().getCookies()
                            .forEach(System.out::println);
                    if (resp.statusCode() != 200)
                        throw new RuntimeException("Login falhou: " + resp.body());
                    return null;
                })
                .thenCompose(v -> {
                    HttpRequest getFuncReq = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/auth/getAllFuncionarios"))
                            .GET()
                            .build();
                    return http.sendAsync(getFuncReq, HttpResponse.BodyHandlers.ofString());
                })
                .thenAccept(resp -> {
                    System.out.println("Funcionários -> " + resp.body());
                    // troca de cena tem de correr no FX Thread
                    Platform.runLater(this::mostrarPaginaBranca);
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void mostrarPaginaBranca() {
        Button testarBtn = new Button("Testar");
        Button logoutBtn = new Button("Logout");

        testarBtn.setOnAction(e -> imprimir());
        logoutBtn.setOnAction(e -> logout());

        // Create a container for the logout button positioned at top right
        HBox topBar = new HBox(logoutBtn);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));

        // Main content in the center
        VBox centerContent = new VBox(20, testarBtn);
        centerContent.setAlignment(Pos.CENTER);

        // Main layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerContent);

        Scene novaCena = new Scene(root, 400, 350);
        primaryStage.setTitle("EatEase");
        primaryStage.setScene(novaCena);
    }

    private void logout() {
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/auth/logout"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        http.sendAsync(loginReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    System.out.println(resp.body());
                    return null;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });

        cookieManager.getCookieStore().removeAll();
        start(primaryStage);
    }

    // exemplo de request depois de ter cookies
    private void imprimir() {
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/auth/getAllFuncionarios"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        http.sendAsync(loginReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    System.out.println(resp.body());
                    return null;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public static void main(String[] args) {
        launch();
    }
}
