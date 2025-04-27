package com.EatEaseFrontend;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * View para gerenciar e exibir funcionários
 */
public class EmployeeView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

    /**
     * Construtor da view de funcionários
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public EmployeeView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
    }

    /**
     * Carrega e exibe a lista de funcionários
     */
    public void show() {
        System.out.println("Carregando lista de funcionários...");

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando funcionários...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get employees
        HttpRequest getFuncReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/getAllFuncionarios")))
                .GET()
                .build();

        httpClient.sendAsync(getFuncReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        System.out.println("Funcionários -> " + resp.body());
                        List<Employee> employees = JsonParser.parseEmployees(resp.body());

                        Platform.runLater(() -> {
                            displayEmployeesAsCards(employees);
                        });
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar funcionários");
                            alert.setContentText("Status code: " + resp.statusCode());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao carregar funcionários");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Exibe os funcionários como cards em um FlowPane
     * 
     * @param employees Lista de funcionários a serem exibidos
     */
    private void displayEmployeesAsCards(List<Employee> employees) {
        contentArea.getChildren().clear();

        // Create scroll pane for employee cards
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));

        // Use FlowPane for responsive cards layout
        FlowPane employeeCards = new FlowPane();
        employeeCards.setHgap(20);
        employeeCards.setVgap(20);
        employeeCards.setPadding(new Insets(20));

        // Add section header
        VBox contentBox = new VBox(20);
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label headerLabel = new Label("Funcionários");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerBox.getChildren().add(headerLabel);

        contentBox.getChildren().add(headerBox);

        // Add employee cards
        if (employees.isEmpty()) {
            Label noEmployeesLabel = new Label("Nenhum funcionário encontrado");
            noEmployeesLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noEmployeesLabel);
        } else {
            for (Employee employee : employees) {
                VBox card = createEmployeeCard(employee);
                employeeCards.getChildren().add(card);
            }
            contentBox.getChildren().add(employeeCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Cria um card para um funcionário
     * 
     * @param employee Funcionário para o qual criar o card
     * @return VBox contendo o card do funcionário
     */
    private VBox createEmployeeCard(Employee employee) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(300);
        card.setPrefHeight(200);
        card.setPadding(new Insets(15));

        // Employee name as card title
        Label nameLabel = new Label(employee.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.getStyleClass().add("card-title");

        // Employee information
        Label usernameLabel = new Label("Username: " + employee.getUsername());
        Label emailLabel = new Label("Email: " + employee.getEmail());
        Label phoneLabel = new Label("Telefone: " + employee.getTelefone());

        // Create a badge for the role
        HBox roleBox = new HBox(5);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label(employee.getCargoName());
        roleLabel.setPadding(new Insets(5, 10, 5, 10));
        roleLabel.setTextFill(Color.WHITE);

        // Set background color based on role
        switch (employee.getCargoId()) {
            case 1: // Funcionário
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#5bc0de"), new CornerRadii(4), Insets.EMPTY)));
                break;
            case 2: // Administrador
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#d9534f"), new CornerRadii(4), Insets.EMPTY)));
                break;
            case 3: // Gerente
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#f0ad4e"), new CornerRadii(4), Insets.EMPTY)));
                break;
            default: // Outros
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#777777"), new CornerRadii(4), Insets.EMPTY)));
        }

        roleBox.getChildren().add(roleLabel);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, usernameLabel, emailLabel, phoneLabel, roleBox);

        return card;
    }
}