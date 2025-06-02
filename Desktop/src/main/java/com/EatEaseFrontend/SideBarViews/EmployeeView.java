package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Cargo;
import com.EatEaseFrontend.Employee;
import com.EatEaseFrontend.ErrorMessages;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.StageManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
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
                            PopUp.showEmployeeLoadError(resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro",
                                "Falha ao carregar funcionários",
                                "Erro: " + ex.getMessage());
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
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label headerLabel = new Label("Funcionários");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Add Employee button
        Button addEmployeeButton = new Button("Adicionar Funcionário");
        addEmployeeButton.getStyleClass().add("login-button");
        FontIcon addIcon = new FontIcon(MaterialDesign.MDI_PLUS);
        addIcon.setIconColor(Color.WHITE);
        addEmployeeButton.setGraphic(addIcon);
        addEmployeeButton.setOnAction(e -> showAddEmployeeDialog());

        // Spacer to push button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(headerLabel, spacer, addEmployeeButton);

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
            case 2: // Gerente
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#d9534f"), new CornerRadii(4), Insets.EMPTY)));
                break;
            case 3: // Cozinheiro
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#f0ad4e"), new CornerRadii(4), Insets.EMPTY)));
                break;
            case 4: // Limpeza
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#5cb85c"), new CornerRadii(4), Insets.EMPTY)));
                break;
            default: // Outros
                roleLabel.setBackground(new Background(new BackgroundFill(
                        Color.web("#777777"), new CornerRadii(4), Insets.EMPTY)));
        }

        roleBox.getChildren().add(roleLabel);

        // Create buttons container
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        // Edit button
        Button editButton = new Button("");
        editButton.setTooltip(new Tooltip("Editar"));
        FontIcon editIcon = new FontIcon(MaterialDesign.MDI_PENCIL);
        editIcon.setIconColor(Color.BLUE);
        editButton.setGraphic(editIcon);
        editButton.getStyleClass().add("icon-button");
        editButton.setOnAction(e -> showEditEmployeeDialog(employee));

        // Delete button
        Button deleteButton = new Button("");
        deleteButton.setTooltip(new Tooltip("Excluir"));
        FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
        deleteIcon.setIconColor(Color.RED);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(e -> PopUp.showConfirmationPopup(
                Alert.AlertType.CONFIRMATION,
                "Excluir Funcionário",
                "Você tem certeza que deseja excluir este funcionário?",
                "Esta ação não pode ser desfeita.",
                () -> deleteEmployee(employee.getId())));

        // Add buttons to container
        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, usernameLabel, emailLabel, phoneLabel, roleBox, buttonsBox);

        return card;
    }

    /**
     * Exibe o diálogo para adicionar um novo funcionário
     */
    private void showAddEmployeeDialog() {
        // Get primary stage for positioning
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        popupContent.setPrefWidth(450);
        popupContent.setPrefHeight(550);

        // Title
        Label titleLabel = new Label("Adicionar Funcionário");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Header
        Label headerLabel = new Label("Preencha os dados do novo funcionário");
        headerLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        // Form fields
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome completo");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField telefoneField = new TextField();
        telefoneField.setPromptText("Telefone");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Dropdown for cargo
        ComboBox<Cargo> cargoComboBox = new ComboBox<>();
        cargoComboBox.getItems().addAll(Cargo.getAllCargos());
        cargoComboBox.setPromptText("Selecione o cargo");

        // Configure the combo box display
        cargoComboBox.setCellFactory(lv -> new ListCell<Cargo>() {
            @Override
            protected void updateItem(Cargo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        cargoComboBox.setButtonCell(new ListCell<Cargo>() {
            @Override
            protected void updateItem(Cargo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o cargo" : item.getDisplayName());
            }
        });

        // Add fields to grid
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Telefone:"), 0, 3);
        grid.add(telefoneField, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Cargo:"), 0, 5);
        grid.add(cargoComboBox, 1, 5);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> popup.hide());

        Button saveButton = new Button("Registrar");
        saveButton.getStyleClass().add("login-button");
        saveButton.setDisable(true);

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Add all components to popup content
        popupContent.getChildren().addAll(titleLabel, headerLabel, grid, buttonBox);

        popup.getContent().add(popupContent);

        // Focus on name field when opened
        Platform.runLater(() -> nomeField.requestFocus());

        // Validation
        Runnable validateFields = () -> {
            boolean allFieldsFilled = !nomeField.getText().trim().isEmpty() &&
                    !usernameField.getText().trim().isEmpty() &&
                    !emailField.getText().trim().isEmpty() &&
                    !telefoneField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty() &&
                    cargoComboBox.getValue() != null;
            saveButton.setDisable(!allFieldsFilled);
        };

        // Add listeners to all fields
        nomeField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        telefoneField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        cargoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

        // Save button action
        saveButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String telefone = telefoneField.getText().trim();
            String password = passwordField.getText().trim();
            Cargo cargo = cargoComboBox.getValue();

            // Validation for required fields
            if (nome.isEmpty() || username.isEmpty() || email.isEmpty() ||
                    telefone.isEmpty() || password.isEmpty() || cargo == null) {
                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Campos Obrigatórios",
                        ErrorMessages.Validation.REQUIRED_FIELDS,
                        "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            // Email validation
            if (!email.contains("@") || !email.contains(".")) {
                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Email Inválido",
                        ErrorMessages.Validation.INVALID_EMAIL,
                        "Por favor, insira um email válido.");
                return;
            }

            // Password validation
            if (password.length() < 6) {
                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Senha Inválida",
                        ErrorMessages.Validation.PASSWORD_TOO_SHORT,
                        "Por favor, insira uma senha com pelo menos 6 caracteres.");
                return;
            }

            if (cargo != null) {
                // Register the employee
                registerEmployee(nome, username, email, telefone, password, cargo.getId());
                popup.hide();
            }
        });

        // Show popup centered
        popup.show(primary, centerX - 225, centerY - 275);
    }

    /**
     * Registra um novo funcionário
     * 
     * @param nome     Nome do funcionário
     * @param username Username do funcionário
     * @param email    Email do funcionário
     * @param telefone Telefone do funcionário
     * @param password Password do funcionário
     * @param cargoId  ID do cargo do funcionário
     */
    private void registerEmployee(String nome, String username, String email, String telefone, String password,
            int cargoId) {
        // Create JSON body
        String jsonBody = "{"
                + "\"nome\":\"" + nome + "\","
                + "\"username\":\"" + username + "\","
                + "\"email\":\"" + email + "\","
                + "\"telefone\":\"" + telefone + "\","
                + "\"password\":\"" + password + "\","
                + "\"cargoId\":" + cargoId
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Register response status: " + response.statusCode());
                    System.out.println("Register response body: " + response.body());
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return true;
                    } else {
                        throw new RuntimeException(
                                "Falha ao registrar funcionário: " + response.statusCode() + " - " + response.body());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload employees after registration
                        show();
                        PopUp.showEmployeeCreateSuccess();
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        PopUp.showEmployeeCreateError(e.getMessage());
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Exibe o diálogo para editar um funcionário existente
     */
    private void showEditEmployeeDialog(Employee employee) {
        // Get primary stage for positioning
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        popupContent.setPrefWidth(450);
        popupContent.setPrefHeight(600);
        // Title
        Label titleLabel = new Label("Editar Funcionário");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        // Nome
        Label nomeLabel = new Label("Nome:");
        TextField nomeField = new TextField(employee.getNome());
        nomeField.setPromptText("Digite o nome");

        // Username
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField(employee.getUsername());
        usernameField.setPromptText("Digite o username");

        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(employee.getEmail());
        emailField.setPromptText("Digite o email");

        // Telefone
        Label telefoneLabel = new Label("Telefone:");
        TextField telefoneField = new TextField(employee.getTelefone());
        telefoneField.setPromptText("Digite o telefone");

        // Password
        Label passwordLabel = new Label("Nova Senha:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Digite a nova senha");

        // Cargo
        Label cargoLabel = new Label("Cargo:");
        ComboBox<Cargo> cargoComboBox = new ComboBox<>();
        cargoComboBox.getItems().addAll(Cargo.getAllCargos());
        cargoComboBox.setValue(Cargo.getById(employee.getCargoId()));

        // Add fields to grid
        formGrid.add(nomeLabel, 0, 0);
        formGrid.add(nomeField, 1, 0);
        formGrid.add(usernameLabel, 0, 1);
        formGrid.add(usernameField, 1, 1);
        formGrid.add(emailLabel, 0, 2);
        formGrid.add(emailField, 1, 2);
        formGrid.add(telefoneLabel, 0, 3);
        formGrid.add(telefoneField, 1, 3);
        formGrid.add(passwordLabel, 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(cargoLabel, 0, 5);
        formGrid.add(cargoComboBox, 1, 5);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            // Validation
            if (nomeField.getText().trim().isEmpty() ||
                    usernameField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty() ||
                    telefoneField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty() ||
                    cargoComboBox.getValue() == null) {

                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Campos Obrigatórios",
                        "Todos os campos são obrigatórios",
                        "Por favor, preencha todos os campos obrigatórios.");
                return;
            }

            // Email validation
            if (!emailField.getText().contains("@") || !emailField.getText().contains(".")) {
                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Email Inválido",
                        "Formato de email inválido",
                        "Por favor, insira um email válido.");
                return;
            }

            // Password validation
            if (passwordField.getText().trim().length() < 6) {
                PopUp.showPopupDialog(Alert.AlertType.WARNING, "Senha Inválida",
                        "A senha deve ter pelo menos 6 caracteres",
                        "Por favor, insira uma senha com pelo menos 6 caracteres.");
                return;
            }

            // Update employee
            updateEmployee(employee.getId(),
                    nomeField.getText().trim(),
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    telefoneField.getText().trim(),
                    passwordField.getText().trim(),
                    cargoComboBox.getValue().getId());
            popup.hide();
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelButton.setOnAction(e -> popup.hide());

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // Add all components to popup content
        popupContent.getChildren().addAll(titleLabel, formGrid, buttonBox);

        popup.getContent().add(popupContent);
        popup.show(primary, centerX - 225, centerY - 275);
    }

    /**
     * Atualiza um funcionário existente
     */
    private void updateEmployee(int employeeId, String nome, String username, String email, String telefone,
            String password,
            int cargoId) {
        // Create JSON body
        String jsonBody = "{"
                + "\"nome\":\"" + nome + "\","
                + "\"username\":\"" + username + "\","
                + "\"email\":\"" + email + "\","
                + "\"telefone\":\"" + telefone + "\","
                + "\"cargoId\":" + cargoId + ","
                + "\"password\":\"" + password + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/updateFuncionario?funcionarioId=" + employeeId)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Update response status: " + response.statusCode());
                    System.out.println("Update response body: " + response.body());
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return true;
                    } else {
                        throw new RuntimeException(
                                "Falha ao atualizar funcionário: " + response.statusCode() + " - " + response.body());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload employees after update
                        show();
                        PopUp.showEmployeeUpdateSuccess();
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        PopUp.showEmployeeUpdateError(e.getMessage());
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Exclui um funcionário
     */
    private void deleteEmployee(int employeeId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/auth/deleteFuncionario?funcionarioId=" + employeeId)))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Delete response status: " + response.statusCode());
                    System.out.println("Delete response body: " + response.body());
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        return true;
                    } else {
                        throw new RuntimeException(
                                "Falha ao excluir funcionário: " + response.statusCode() + " - " + response.body());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload employees after deletion
                        show();
                        PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso",
                                "Funcionário excluído",
                                "O funcionário foi excluído com sucesso!");
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro",
                                "Falha ao excluir funcionário",
                                "Erro: " + e.getMessage());
                    });
                    e.printStackTrace();
                    return null;
                });
    }
}