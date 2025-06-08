package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.StageManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * View para gerenciar e exibir ingredientes
 */
public class IngredientsView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private List<Ingredient> allIngredients; // Store all ingredients for filtering
    private TextField searchField; // Search field reference

    // Map to store unit IDs and their corresponding names
    private static final Map<Integer, String> UNIDADE_MAP = new HashMap<>();
    private static final List<String> UNIDADE_NAMES = Arrays.asList(
            "Selecione uma unidade", // placeholder for index 0
            "Quilos",
            "Gramas",
            "Litros",
            "Mililitros",
            "Unidades",
            "Doses",
            "Caixas");

    /**
     * Construtor da view de ingredientes
     *
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public IngredientsView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;

        // Initialize the unit mapping
        for (int i = 1; i < UNIDADE_NAMES.size(); i++) {
            UNIDADE_MAP.put(i, UNIDADE_NAMES.get(i));
        }
    }

    /**
     * Get the name of the unit from its ID
     * 
     * @param unidadeId ID of the unit
     * @return Name of the unit or "Desconhecido" if not found
     */
    private String getUnidadeName(int unidadeId) {
        return UNIDADE_MAP.getOrDefault(unidadeId, "Desconhecido");
    }

    /**
     * Carrega e exibe a lista de ingredientes
     */
    public void show() {
        System.out.println("Carregando lista de ingredientes...");

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando ingredientes...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get ingredients
        HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        System.out.println("Ingredientes -> " + resp.body());
                        List<Ingredient> ingredients = JsonParser.parseIngredients(resp.body());
                        allIngredients = ingredients; // Store all ingredients

                        Platform.runLater(() -> {
                            displayIngredientsAsCards(ingredients);
                        });
                    } else {
                        Platform.runLater(() -> {
                            PopUp.showIngredientLoadError(resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        PopUp.showIngredientLoadError(ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Exibe os ingredientes como cards em um FlowPane
     *
     * @param ingredients Lista de ingredientes a serem exibidos
     */
    private void displayIngredientsAsCards(List<Ingredient> ingredients) {
        contentArea.getChildren().clear();

        // Create scroll pane for ingredient cards
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));

        // Use FlowPane for responsive cards layout
        FlowPane ingredientCards = new FlowPane();
        ingredientCards.setHgap(20);
        ingredientCards.setVgap(20);
        ingredientCards.setPadding(new Insets(20));

        // Add section header
        VBox contentBox = new VBox(20);
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label headerLabel = new Label("Ingredientes");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerBox.getChildren().add(headerLabel);

        // Create search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 20, 0));

        searchField = new TextField();
        searchField.setPromptText("Pesquisar ingredientes...");
        searchField.setPrefWidth(300);
        searchField.setMaxWidth(300);

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIngredients(newValue);
        });

        searchBox.getChildren().add(searchField);

        // Adicionar botão "Adicionar"
        Button addButton = new Button("Adicionar");
        addButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLUS));
        addButton.getStyleClass().add("login-button"); // Use the same style as login button for consistency
        addButton.setOnAction(e -> showAddIngredientPopup());

        // Create a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, addButton);

        contentBox.getChildren().addAll(headerBox, searchBox);

        // Add ingredient cards
        if (ingredients.isEmpty()) {
            Label noIngredientsLabel = new Label("Nenhum ingrediente encontrado");
            noIngredientsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noIngredientsLabel);
        } else {
            // Sort ingredients alphabetically by name
            List<Ingredient> sortedIngredients = ingredients.stream()
                    .sorted((i1, i2) -> i1.getNome().compareToIgnoreCase(i2.getNome()))
                    .collect(Collectors.toList());

            for (Ingredient ingredient : sortedIngredients) {
                VBox card = createIngredientCard(ingredient);
                ingredientCards.getChildren().add(card);
            }
            contentBox.getChildren().add(ingredientCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Filters the ingredients based on the search query
     * 
     * @param searchQuery The text to search for in ingredient names
     */
    private void filterIngredients(String searchQuery) {
        if (allIngredients == null) {
            return;
        }

        List<Ingredient> filteredIngredients;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // Show all ingredients if search is empty
            filteredIngredients = allIngredients;
        } else {
            // Filter ingredients that contain the search query (case insensitive)
            String lowerCaseQuery = searchQuery.toLowerCase().trim();
            filteredIngredients = allIngredients.stream()
                    .filter(ingredient -> ingredient.getNome().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        // Update the display with filtered ingredients
        displayFilteredIngredients(filteredIngredients);
    }

    /**
     * Displays the filtered ingredients without affecting the search bar
     * 
     * @param ingredients List of filtered ingredients to display
     */
    private void displayFilteredIngredients(List<Ingredient> ingredients) {
        // Find the content box and update only the ingredient cards part
        ScrollPane scrollPane = (ScrollPane) contentArea.getChildren().get(0);
        VBox contentBox = (VBox) scrollPane.getContent();

        // Remove existing ingredient cards (keep header and search bar)
        if (contentBox.getChildren().size() > 2) {
            contentBox.getChildren().remove(2, contentBox.getChildren().size());
        }

        // Create new FlowPane for ingredient cards
        FlowPane ingredientCards = new FlowPane();
        ingredientCards.setHgap(20);
        ingredientCards.setVgap(20);
        ingredientCards.setPadding(new Insets(20));

        // Add ingredient cards
        if (ingredients.isEmpty()) {
            Label noIngredientsLabel = new Label("Nenhum ingrediente encontrado");
            noIngredientsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noIngredientsLabel);
        } else {
            // Sort ingredients alphabetically by name
            List<Ingredient> sortedIngredients = ingredients.stream()
                    .sorted((i1, i2) -> i1.getNome().compareToIgnoreCase(i2.getNome()))
                    .collect(Collectors.toList());

            for (Ingredient ingredient : sortedIngredients) {
                VBox card = createIngredientCard(ingredient);
                ingredientCards.getChildren().add(card);
            }
            contentBox.getChildren().add(ingredientCards);
        }
    }

    /**
     * Cria um card para um ingrediente
     *
     * @param ingredient Ingrediente para o qual criar o card
     * @return VBox contendo o card do ingrediente
     */
    private VBox createIngredientCard(Ingredient ingredient) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(300);
        card.setPrefHeight(180);
        card.setPadding(new Insets(15));

        // Ingredient name as card title
        Label nameLabel = new Label(ingredient.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.getStyleClass().add("card-title");

        // Ingredient information
        Label idLabel = new Label("ID: " + ingredient.getId());

        // Stock information with visual indicator
        HBox stockBox = new HBox(10);
        stockBox.setAlignment(Pos.CENTER_LEFT);

        Label stockLabel = new Label("Stock: " + ingredient.getStock());
        Label stockMinLabel = new Label("Stock Mínimo: " + ingredient.getStock_min());

        // Add visual indicator for stock level
        Region stockIndicator = new Region();
        stockIndicator.setPrefWidth(15);
        stockIndicator.setPrefHeight(15);

        // Set color based on stock level
        Color indicatorColor;
        if (ingredient.getStock() <= ingredient.getStock_min() * 0.5) {
            // Critical - Red
            indicatorColor = Color.RED;
        } else if (ingredient.getStock() <= ingredient.getStock_min()) {
            // Low - Orange
            indicatorColor = Color.ORANGE;
        } else {
            // Good - Green
            indicatorColor = Color.GREEN;
        }

        stockIndicator.setBackground(new Background(new BackgroundFill(
                indicatorColor, new CornerRadii(7.5), Insets.EMPTY)));

        stockBox.getChildren().addAll(stockIndicator, stockLabel);

        // Unit information
        Label unitLabel = new Label("Unidade: " + getUnidadeName(ingredient.getUnidade_id()));

        // Stock control buttons
        HBox stockControlBox = new HBox(5);
        stockControlBox.setAlignment(Pos.CENTER_LEFT);
        stockControlBox.setPadding(new Insets(5, 0, 0, 0));

        // Decrease stock button
        Button decreaseButton = new Button("");
        decreaseButton.setTooltip(new Tooltip("Remover Stock"));
        FontIcon decreaseIcon = new FontIcon(MaterialDesign.MDI_MINUS);
        decreaseIcon.setIconColor(Color.RED);
        decreaseButton.setGraphic(decreaseIcon);
        decreaseButton.getStyleClass().add("icon-button");
        decreaseButton.setOnAction(e -> showStockMovementPopup(ingredient, false));

        // Increase stock button
        Button increaseButton = new Button("");
        increaseButton.setTooltip(new Tooltip("Adicionar Stock"));
        FontIcon increaseIcon = new FontIcon(MaterialDesign.MDI_PLUS);
        increaseIcon.setIconColor(Color.GREEN);
        increaseButton.setGraphic(increaseIcon);
        increaseButton.getStyleClass().add("icon-button");
        increaseButton.setOnAction(e -> showStockMovementPopup(ingredient, true));

        Label stockControlLabel = new Label("Controle de Stock:");
        stockControlLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        stockControlBox.getChildren().addAll(stockControlLabel, decreaseButton, increaseButton);

        // Add action buttons
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
        editButton.setOnAction(e -> showEditIngredientPopup(ingredient));

        // Delete button
        Button deleteButton = new Button("");
        deleteButton.setTooltip(new Tooltip("Excluir"));
        FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
        deleteIcon.setIconColor(Color.RED);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(e -> PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                "Confirmar Exclusão", "Excluir Ingrediente",
                "Você tem certeza que deseja excluir este ingrediente?",
                () -> deleteIngredient(ingredient.getId())));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, idLabel, stockBox, stockMinLabel, unitLabel, stockControlBox, buttonsBox);

        return card;
    }

    /**
     * Exibe o diálogo para adicionar um novo ingrediente
     */
    private void showAddIngredientPopup() {
        // 1) Pega no primaryStage para posicionar a popup ao centro
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // 2) Cria a Popup
        Popup popup = new Popup();
        popup.setAutoHide(false); // Não fecha ao clicar fora - requer confirmação

        // 3) Conteúdo principal da popup
        VBox popupContent = new VBox(20);
        popupContent.setPadding(new Insets(25));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12,0,0,4);");
        popupContent.setPrefWidth(450);
        popupContent.setMaxWidth(450);

        // 4) Header com ícone e título
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon headerIcon = new FontIcon(MaterialDesign.MDI_PLUS_CIRCLE);
        headerIcon.setIconColor(Color.valueOf("#FB8C00"));
        headerIcon.setIconSize(24);

        Label titleLabel = new Label("Adicionar Ingrediente");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // Spacer para empurrar o botão X para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botão X para fechar
        Button closeBtn = new Button("");
        FontIcon closeIcon = new FontIcon(MaterialDesign.MDI_CLOSE);
        closeIcon.setIconColor(Color.valueOf("#666666"));
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.getStyleClass().add("icon-button");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 5;" +
                        "-fx-cursor: hand;");

        headerBox.getChildren().addAll(headerIcon, titleLabel, spacer, closeBtn);

        // 5) Seção de campos do formulário
        VBox formSection = new VBox(15);

        // Campo Nome
        VBox nameSection = new VBox(5);
        Label nameLabel = new Label("Nome do Ingrediente");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.valueOf("#666666"));

        TextField nameField = new TextField();
        nameField.setPromptText("Digite o nome do ingrediente...");
        nameField.setPrefHeight(40);
        nameField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        nameSection.getChildren().addAll(nameLabel, nameField);

        // Campo Stock
        VBox stockSection = new VBox(5);
        Label stockLabel = new Label("Quantidade em Stock");
        stockLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        stockLabel.setTextFill(Color.valueOf("#666666"));

        TextField stockField = new TextField();
        stockField.setPromptText("Digite a quantidade inicial...");
        stockField.setPrefHeight(40);
        stockField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        stockField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockField.setText(n.replaceAll("[^\\d]", ""));
        });
        stockSection.getChildren().addAll(stockLabel, stockField);

        // Campo Stock Mínimo
        VBox stockMinSection = new VBox(5);
        Label stockMinLabel = new Label("Stock Mínimo");
        stockMinLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        stockMinLabel.setTextFill(Color.valueOf("#666666"));

        TextField stockMinField = new TextField();
        stockMinField.setPromptText("Digite o stock mínimo...");
        stockMinField.setPrefHeight(40);
        stockMinField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        stockMinField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockMinField.setText(n.replaceAll("[^\\d]", ""));
        });
        stockMinSection.getChildren().addAll(stockMinLabel, stockMinField);

        // Campo Unidade
        VBox unidadeSection = new VBox(5);
        Label unidadeLabel = new Label("Unidade de Medida");
        unidadeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        unidadeLabel.setTextFill(Color.valueOf("#666666"));

        ComboBox<String> unidadeCombo = new ComboBox<>();
        unidadeCombo.getItems().addAll(UNIDADE_NAMES.subList(1, UNIDADE_NAMES.size()));
        unidadeCombo.setPromptText("Selecione a unidade...");
        unidadeCombo.setPrefHeight(40);
        unidadeCombo.setPrefWidth(Double.MAX_VALUE);
        unidadeCombo.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");
        unidadeSection.getChildren().addAll(unidadeLabel, unidadeCombo);

        formSection.getChildren().addAll(nameSection, stockSection, stockMinSection, unidadeSection);

        // Definir ação do botão X após os campos estarem criados
        closeBtn.setOnAction(e -> {
            // Verificar se há campos preenchidos antes de fechar
            boolean hasData = !nameField.getText().trim().isEmpty() ||
                    !stockField.getText().trim().isEmpty() ||
                    !stockMinField.getText().trim().isEmpty() ||
                    unidadeCombo.getValue() != null;

            if (hasData) {
                PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                        "Confirmar Fechamento", "Fechar Diálogo",
                        "Existem dados preenchidos. Tem certeza que deseja fechar?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        // 6) Botões com styling melhorado
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #6C757D;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        Button addBtn = new Button("Adicionar");
        addBtn.setPrefWidth(100);
        addBtn.setPrefHeight(40);
        addBtn.setDisable(true);
        addBtn.setStyle(
                "-fx-background-color: #FB8C00;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        // Hover effects
        cancelBtn.setOnMouseEntered(e -> {
            cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #5A6268;");
        });
        cancelBtn.setOnMouseExited(e -> {
            cancelBtn.setStyle(
                    "-fx-background-color: #6C757D;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;");
        });

        addBtn.setOnMouseEntered(e -> {
            if (!addBtn.isDisabled()) {
                addBtn.setStyle(
                        "-fx-background-color: #F57C00;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });
        addBtn.setOnMouseExited(e -> {
            if (!addBtn.isDisabled()) {
                addBtn.setStyle(
                        "-fx-background-color: #FB8C00;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);

        // 7) Validação em tempo real com feedback visual
        ChangeListener<String> valida = (obs, o, n) -> {
            boolean nameOk = !nameField.getText().trim().isEmpty();
            boolean stockOk = !stockField.getText().trim().isEmpty();
            boolean stockMinOk = !stockMinField.getText().trim().isEmpty();
            boolean unidadeOk = unidadeCombo.getValue() != null;

            boolean allOk = nameOk && stockOk && stockMinOk && unidadeOk;
            addBtn.setDisable(!allOk);

            // Feedback visual nos campos
            updateFieldStyle(nameField, nameOk || nameField.getText().trim().isEmpty());
            updateFieldStyle(stockField, stockOk || stockField.getText().trim().isEmpty());
            updateFieldStyle(stockMinField, stockMinOk || stockMinField.getText().trim().isEmpty());
        };

        nameField.textProperty().addListener(valida);
        stockField.textProperty().addListener(valida);
        stockMinField.textProperty().addListener(valida);
        unidadeCombo.valueProperty().addListener((o, ov, nv) -> valida.changed(null, null, null));

        // 8) Montar o layout
        popupContent.getChildren().addAll(headerBox, formSection, buttonBox);

        // 9) Adiciona o conteúdo à popup
        popup.getContent().add(popupContent);

        // 10) Mostra a popup centrada
        popup.show(primary, centerX - 225, centerY - 200);

        // 11) Ações dos botões
        cancelBtn.setOnAction(e -> {
            // Verificar se há campos preenchidos antes de cancelar
            boolean hasData = !nameField.getText().trim().isEmpty() ||
                    !stockField.getText().trim().isEmpty() ||
                    !stockMinField.getText().trim().isEmpty() ||
                    unidadeCombo.getValue() != null;

            if (hasData) {
                PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                        "Confirmar Cancelamento", "Cancelar Adição",
                        "Existem dados preenchidos. Tem certeza que deseja cancelar?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        addBtn.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int stock = Integer.parseInt(stockField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            String unidade = unidadeCombo.getValue();
            createIngredient(nome, stock, stockMin, unidade);
            popup.hide();
        });

        // 12) Focus no primeiro campo
        Platform.runLater(() -> nameField.requestFocus());
    }

    // Método auxiliar para atualizar estilo dos campos
    private void updateFieldStyle(TextField field, boolean isValid) {
        String baseStyle = "-fx-font-size: 14px;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 12;";

        if (isValid) {
            field.setStyle(baseStyle + "-fx-border-color: #E0E0E0;");
        } else {
            field.setStyle(baseStyle + "-fx-border-color: #F44336;");
        }
    }

    /**
     * Envia uma requisição para a API para criar um novo ingrediente
     *
     * @param nome          Nome do ingrediente
     * @param stock         Quantidade em stock
     * @param stockMin      Quantidade mínima em stock
     * @param unidadeMedida Unidade de medida do ingrediente
     */
    private void createIngredient(String nome, int stock, int stockMin, String unidadeMedida) {
        unidadeMedida = unidadeMedida.toLowerCase();

        // Criar JSON para a requisição exatamente no formato solicitado
        String jsonBody = String.format(
                "{\"nome\":\"%s\",\"stock\":%d,\"stock_min\":%d,\"unidadeMedida\":\"%s\"}",
                nome, stock, stockMin, unidadeMedida);

        System.out.println("Enviando requisição para criar ingrediente: " + jsonBody);

        // Criar a requisição HTTP
        HttpRequest createIngredientReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/create")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(createIngredientReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                        System.out.println("Ingrediente criado com sucesso: " + resp.body());

                        // Recarregar a lista de ingredientes
                        Platform.runLater(() -> {
                            show(); // Reload all ingredients
                            // Clear search field to show all ingredients
                            if (searchField != null) {
                                searchField.clear();
                            }
                        });

                        // Mostrar mensagem de sucesso
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Ingrediente adicionado!",
                                    "O ingrediente foi criado com sucesso.");
                        });
                    } else {
                        System.err.println("Erro ao criar ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            PopUp.showIngredientAddError(resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        PopUp.showIngredientAddError(ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Exibe o diálogo para editar um ingrediente existente
     *
     * @param ingredient Ingrediente a ser editado
     */
    private void showEditIngredientPopup(Ingredient ingredient) {
        // 1) Pega no primaryStage para posicionar o popup ao centro
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // 2) Cria o Popup
        Popup popup = new Popup();
        popup.setAutoHide(false); // Não fecha ao clicar fora - requer confirmação

        // 3) Conteúdo principal da popup
        VBox popupContent = new VBox(20);
        popupContent.setPadding(new Insets(25));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12,0,0,4);");
        popupContent.setPrefWidth(450);
        popupContent.setMaxWidth(450);

        // 4) Header com ícone e título
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon headerIcon = new FontIcon(MaterialDesign.MDI_PENCIL);
        headerIcon.setIconColor(Color.valueOf("#FB8C00"));
        headerIcon.setIconSize(24);

        Label titleLabel = new Label("Editar Ingrediente");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // Spacer para empurrar o botão X para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botão X para fechar
        Button closeBtn = new Button("");
        FontIcon closeIcon = new FontIcon(MaterialDesign.MDI_CLOSE);
        closeIcon.setIconColor(Color.valueOf("#666666"));
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.getStyleClass().add("icon-button");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 5;" +
                        "-fx-cursor: hand;");

        headerBox.getChildren().addAll(headerIcon, titleLabel, spacer, closeBtn);

        // 5) Informação do ingrediente atual
        VBox currentInfoCard = new VBox(8);
        currentInfoCard.setPadding(new Insets(15));
        currentInfoCard.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");

        Label currentInfoLabel = new Label("Editando: " + ingredient.getNome());
        currentInfoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        currentInfoLabel.setTextFill(Color.valueOf("#333333"));

        Label currentIdLabel = new Label("ID: " + ingredient.getId());
        currentIdLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        currentIdLabel.setTextFill(Color.valueOf("#666666"));

        currentInfoCard.getChildren().addAll(currentInfoLabel, currentIdLabel);

        // 6) Seção de campos do formulário
        VBox formSection = new VBox(15);

        // Campo Nome
        VBox nameSection = new VBox(5);
        Label nameLabel = new Label("Nome do Ingrediente");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        nameLabel.setTextFill(Color.valueOf("#666666"));

        TextField nameField = new TextField(ingredient.getNome());
        nameField.setPrefHeight(40);
        nameField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        nameSection.getChildren().addAll(nameLabel, nameField);

        // Campo Stock
        VBox stockSection = new VBox(5);
        Label stockLabel = new Label("Quantidade em Stock");
        stockLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        stockLabel.setTextFill(Color.valueOf("#666666"));

        TextField stockField = new TextField(String.valueOf(ingredient.getStock()));
        stockField.setPrefHeight(40);
        stockField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        stockField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockField.setText(n.replaceAll("[^\\d]", ""));
        });
        stockSection.getChildren().addAll(stockLabel, stockField);

        // Campo Stock Mínimo
        VBox stockMinSection = new VBox(5);
        Label stockMinLabel = new Label("Stock Mínimo");
        stockMinLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        stockMinLabel.setTextFill(Color.valueOf("#666666"));

        TextField stockMinField = new TextField(String.valueOf(ingredient.getStock_min()));
        stockMinField.setPrefHeight(40);
        stockMinField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");
        stockMinField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockMinField.setText(n.replaceAll("[^\\d]", ""));
        });
        stockMinSection.getChildren().addAll(stockMinLabel, stockMinField);

        // Campo Unidade
        VBox unidadeSection = new VBox(5);
        Label unidadeLabel = new Label("Unidade de Medida");
        unidadeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        unidadeLabel.setTextFill(Color.valueOf("#666666"));

        ComboBox<String> unidadeCombo = new ComboBox<>();
        unidadeCombo.getItems().addAll(UNIDADE_NAMES.subList(1, UNIDADE_NAMES.size()));
        unidadeCombo.setValue(getUnidadeName(ingredient.getUnidade_id()));
        unidadeCombo.setPrefHeight(40);
        unidadeCombo.setPrefWidth(Double.MAX_VALUE);
        unidadeCombo.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");
        unidadeSection.getChildren().addAll(unidadeLabel, unidadeCombo);

        formSection.getChildren().addAll(nameSection, stockSection, stockMinSection, unidadeSection);

        // Definir ação do botão X após os campos estarem criados
        closeBtn.setOnAction(e -> {
            // Verificar se houve alterações nos campos antes de fechar
            boolean hasChanges = !nameField.getText().trim().equals(ingredient.getNome()) ||
                    !stockField.getText().equals(String.valueOf(ingredient.getStock())) ||
                    !stockMinField.getText().equals(String.valueOf(ingredient.getStock_min())) ||
                    !unidadeCombo.getValue().equals(getUnidadeName(ingredient.getUnidade_id()));

            if (hasChanges) {
                PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                        "Confirmar Fechamento", "Fechar Diálogo",
                        "Existem alterações não guardadas. Tem certeza que deseja fechar?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        // 7) Botões com styling melhorado
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #6C757D;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        Button saveBtn = new Button("Guardar");
        saveBtn.setPrefWidth(100);
        saveBtn.setPrefHeight(40);
        saveBtn.setDisable(true);
        saveBtn.setStyle(
                "-fx-background-color: #FB8C00;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        // Hover effects
        cancelBtn.setOnMouseEntered(e -> {
            cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #5A6268;");
        });
        cancelBtn.setOnMouseExited(e -> {
            cancelBtn.setStyle(
                    "-fx-background-color: #6C757D;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;");
        });

        saveBtn.setOnMouseEntered(e -> {
            if (!saveBtn.isDisabled()) {
                saveBtn.setStyle(
                        "-fx-background-color: #F57C00;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });
        saveBtn.setOnMouseExited(e -> {
            if (!saveBtn.isDisabled()) {
                saveBtn.setStyle(
                        "-fx-background-color: #FB8C00;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        // 8) Validação em tempo real com feedback visual
        ChangeListener<String> valida = (obs, o, n) -> {
            boolean nameOk = !nameField.getText().trim().isEmpty();
            boolean stockOk = !stockField.getText().trim().isEmpty();
            boolean stockMinOk = !stockMinField.getText().trim().isEmpty();
            boolean unidadeOk = unidadeCombo.getValue() != null;

            boolean allOk = nameOk && stockOk && stockMinOk && unidadeOk;
            saveBtn.setDisable(!allOk);

            // Feedback visual nos campos
            updateFieldStyle(nameField, nameOk || nameField.getText().trim().isEmpty());
            updateFieldStyle(stockField, stockOk || stockField.getText().trim().isEmpty());
            updateFieldStyle(stockMinField, stockMinOk || stockMinField.getText().trim().isEmpty());
        };

        nameField.textProperty().addListener(valida);
        stockField.textProperty().addListener(valida);
        stockMinField.textProperty().addListener(valida);
        unidadeCombo.valueProperty().addListener((o, ov, nv) -> valida.changed(null, null, null));

        // 9) Montar o layout
        popupContent.getChildren().addAll(headerBox, currentInfoCard, formSection, buttonBox);

        // 10) Adiciona o conteúdo à popup
        popup.getContent().add(popupContent);

        // 11) Mostra a popup centrada
        popup.show(primary, centerX - 225, centerY - 220);

        // 12) Ações dos botões
        cancelBtn.setOnAction(e -> {
            // Verificar se houve alterações nos campos antes de cancelar
            boolean hasChanges = !nameField.getText().trim().equals(ingredient.getNome()) ||
                    !stockField.getText().equals(String.valueOf(ingredient.getStock())) ||
                    !stockMinField.getText().equals(String.valueOf(ingredient.getStock_min())) ||
                    !unidadeCombo.getValue().equals(getUnidadeName(ingredient.getUnidade_id()));

            if (hasChanges) {
                PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                        "Confirmar Cancelamento", "Cancelar Edição",
                        "Existem alterações não guardadas. Tem certeza que deseja cancelar?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        saveBtn.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int stock = Integer.parseInt(stockField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            String unidade = unidadeCombo.getValue();
            updateIngredient(ingredient.getId(), nome, stock, stockMin, unidade);
            popup.hide();
        });

        // 13) Focus no primeiro campo
        Platform.runLater(() -> nameField.requestFocus());
    }

    /**
     * Envia uma requisição para a API para atualizar um ingrediente existente
     * 
     * @param id            ID do ingrediente a ser atualizado
     * @param nome          Nome do ingrediente
     * @param stock         Quantidade em stock
     * @param stockMin      Quantidade mínima em stock
     * @param unidadeMedida Unidade de medida do ingrediente
     */
    private void updateIngredient(int id, String nome, int stock, int stockMin, String unidadeMedida) {
        unidadeMedida = unidadeMedida.toLowerCase();
        // Criar JSON para a requisição
        String jsonBody = String.format(
                "{\"nome\":\"%s\",\"stock\":%d,\"stock_min\":%d,\"unidadeMedida\":\"%s\"}",
                nome, stock, stockMin, unidadeMedida);

        System.out.println("Enviando requisição para atualizar ingrediente: " + jsonBody);

        // Criar a requisição HTTP
        HttpRequest updateIngredientReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/edit?id=" + id)))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(updateIngredientReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                        System.out.println("Ingrediente atualizado com sucesso: " + resp.body());

                        // Recarregar a lista de ingredientes
                        Platform.runLater(() -> {
                            show(); // Reload all ingredients
                            // Clear search field to show all ingredients
                            if (searchField != null) {
                                searchField.clear();
                            }
                        });

                        // Mostrar mensagem de sucesso
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Ingrediente atualizado!",
                                    "O ingrediente foi atualizado com sucesso.");
                        });
                    } else {
                        System.err.println("Erro ao atualizar ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar ingrediente",
                                    "Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar ingrediente",
                                "Erro: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Envia uma requisição para a API para excluir um ingrediente
     * 
     * @param id ID do ingrediente a ser excluído
     */
    private void deleteIngredient(int id) {
        System.out.println("Enviando requisição para excluir ingrediente com ID: " + id);

        // Criar a requisição HTTP
        HttpRequest deleteIngredientReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/delete?id=" + id)))
                .DELETE()
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(deleteIngredientReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                        System.out.println("Ingrediente excluído com sucesso: " + resp.body());

                        // Recarregar a lista de ingredientes
                        Platform.runLater(() -> {
                            show(); // Reload all ingredients
                            // Clear search field to show all ingredients
                            if (searchField != null) {
                                searchField.clear();
                            }
                        });

                        // Mostrar mensagem de sucesso
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Ingrediente excluído",
                                    "O ingrediente foi excluído com sucesso.");
                        });
                    } else {
                        System.err.println("Erro ao excluir ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir ingrediente",
                                    "Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir ingrediente",
                                "Erro: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Exibe o popup para movimentação de stock (adicionar ou remover)
     * 
     * @param ingredient Ingrediente para movimentar o stock
     * @param isIncrease true para adicionar, false para remover
     */
    private void showStockMovementPopup(Ingredient ingredient, boolean isIncrease) {
        // 1) Pega no primaryStage para posicionar a popup ao centro
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // 2) Cria a Popup
        Popup popup = new Popup();
        popup.setAutoHide(true); // fecha ao clicar fora

        // 3) Conteúdo principal da popup
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(25));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12,0,0,4);");
        popupContent.setPrefWidth(400);
        popupContent.setMaxWidth(400);

        // 4) Header com ícone e título
        String actionText = isIncrease ? "Adicionar" : "Remover";
        String titleText = isIncrease ? "Adicionar Stock" : "Remover Stock";

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Ícone baseado na ação
        FontIcon headerIcon = new FontIcon(
                isIncrease ? MaterialDesign.MDI_PLUS_CIRCLE : MaterialDesign.MDI_MINUS_CIRCLE);
        headerIcon.setIconColor(isIncrease ? Color.valueOf("#4CAF50") : Color.valueOf("#F44336"));
        headerIcon.setIconSize(24);

        Label titleLabel = new Label(titleText);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        headerBox.getChildren().addAll(headerIcon, titleLabel);

        // 5) Informações do ingrediente em um card
        VBox ingredientInfoCard = new VBox(8);
        ingredientInfoCard.setPadding(new Insets(15));
        ingredientInfoCard.setStyle(
                "-fx-background-color: #F8F9FA;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;");

        Label ingredientLabel = new Label("Ingrediente");
        ingredientLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        ingredientLabel.setTextFill(Color.valueOf("#666666"));

        Label ingredientNameLabel = new Label(ingredient.getNome());
        ingredientNameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        ingredientNameLabel.setTextFill(Color.valueOf("#333333"));

        // Stock atual com indicador visual
        HBox stockInfoBox = new HBox(10);
        stockInfoBox.setAlignment(Pos.CENTER_LEFT);

        Label stockLabel = new Label("Stock Atual:");
        stockLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        stockLabel.setTextFill(Color.valueOf("#666666"));

        Label stockValueLabel = new Label(ingredient.getStock() + " " + getUnidadeName(ingredient.getUnidade_id()));
        stockValueLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Indicador visual do nível de stock
        Region stockIndicator = new Region();
        stockIndicator.setPrefWidth(12);
        stockIndicator.setPrefHeight(12);

        Color indicatorColor;
        if (ingredient.getStock() <= ingredient.getStock_min() * 0.5) {
            indicatorColor = Color.valueOf("#F44336"); // Crítico - Vermelho
        } else if (ingredient.getStock() <= ingredient.getStock_min()) {
            indicatorColor = Color.valueOf("#FF9800"); // Baixo - Laranja
        } else {
            indicatorColor = Color.valueOf("#4CAF50"); // Bom - Verde
        }

        stockIndicator.setBackground(new Background(new BackgroundFill(
                indicatorColor, new CornerRadii(6), Insets.EMPTY)));
        stockValueLabel.setTextFill(indicatorColor);

        stockInfoBox.getChildren().addAll(stockIndicator, stockLabel, stockValueLabel);

        ingredientInfoCard.getChildren().addAll(ingredientLabel, ingredientNameLabel, stockInfoBox);

        // 6) Campo de quantidade com melhor styling
        VBox quantitySection = new VBox(8);

        Label quantityTitleLabel = new Label("Quantidade a " + (isIncrease ? "adicionar" : "remover") + ":");
        quantityTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        quantityTitleLabel.setTextFill(Color.valueOf("#333333"));

        TextField quantityField = new TextField();
        quantityField.setPromptText("Digite a quantidade...");
        quantityField.setPrefHeight(40);
        quantityField.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-border-color: #E0E0E0;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 12;");

        // Validação apenas números
        quantityField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                quantityField.setText(n.replaceAll("[^\\d]", ""));
        });

        quantitySection.getChildren().addAll(quantityTitleLabel, quantityField);

        // 7) Botões com styling melhorado
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancelar");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #6C757D;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        Button actionBtn = new Button(actionText);
        actionBtn.setPrefWidth(100);
        actionBtn.setPrefHeight(40);
        actionBtn.setDisable(true);

        // Estilo do botão de ação baseado no tipo
        String actionButtonColor = isIncrease ? "#4CAF50" : "#F44336";
        actionBtn.setStyle(
                "-fx-background-color: " + actionButtonColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");

        // Hover effects
        cancelBtn.setOnMouseEntered(e -> {
            cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-background-color: #5A6268;");
        });
        cancelBtn.setOnMouseExited(e -> {
            cancelBtn.setStyle(
                    "-fx-background-color: #6C757D;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;");
        });

        String hoverColor = isIncrease ? "#45A049" : "#E53935";
        actionBtn.setOnMouseEntered(e -> {
            if (!actionBtn.isDisabled()) {
                actionBtn.setStyle(
                        "-fx-background-color: " + hoverColor + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });
        actionBtn.setOnMouseExited(e -> {
            if (!actionBtn.isDisabled()) {
                actionBtn.setStyle(
                        "-fx-background-color: " + actionButtonColor + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-cursor: hand;");
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, actionBtn);

        // 8) Validação em tempo real
        ChangeListener<String> valida = (obs, o, n) -> {
            try {
                boolean ok = !quantityField.getText().trim().isEmpty() &&
                        Integer.parseInt(quantityField.getText()) > 0;
                actionBtn.setDisable(!ok);

                // Mudar cor da borda baseado na validação
                if (ok) {
                    quantityField.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-border-color: #FB8C00;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 6;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-padding: 8 12;");
                } else if (!quantityField.getText().trim().isEmpty()) {
                    quantityField.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-border-color: #F44336;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 6;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-padding: 8 12;");
                } else {
                    quantityField.setStyle(
                            "-fx-font-size: 14px;" +
                                    "-fx-border-color: #E0E0E0;" +
                                    "-fx-border-width: 2;" +
                                    "-fx-border-radius: 6;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-padding: 8 12;");
                }
            } catch (NumberFormatException e) {
                actionBtn.setDisable(true);
                quantityField.setStyle(
                        "-fx-font-size: 14px;" +
                                "-fx-border-color: #F44336;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 8 12;");
            }
        };
        quantityField.textProperty().addListener(valida);

        // 9) Montar o layout
        popupContent.getChildren().addAll(headerBox, ingredientInfoCard, quantitySection, buttonBox);

        // 10) Adiciona o conteúdo à popup
        popup.getContent().add(popupContent);

        // 11) Mostra a popup centrada
        popup.show(primary, centerX - 200, centerY - 180);

        // 12) Ações dos botões
        cancelBtn.setOnAction(e -> popup.hide());
        actionBtn.setOnAction(e -> {
            int quantity = Integer.parseInt(quantityField.getText());
            // Se for remover, a quantidade deve ser negativa
            if (!isIncrease) {
                quantity = -quantity;
            }
            moveStock(ingredient.getId(), quantity);
            popup.hide();
        });

        // 13) Focus no campo de quantidade
        Platform.runLater(() -> quantityField.requestFocus());
    }

    /**
     * Envia uma requisição para a API para movimentar o stock de um ingrediente
     * 
     * @param ingredientId ID do ingrediente
     * @param quantidade   Quantidade a ser movimentada (positiva para adicionar,
     *                     negativa para remover)
     */
    private void moveStock(int ingredientId, int quantidade) {
        // Criar JSON para a requisição
        String jsonBody = String.format("{\"quantidade\":%d}", quantidade);

        System.out.println("Enviando requisição para movimentar stock: " + jsonBody);

        // Criar a requisição HTTP
        HttpRequest moveStockReq = HttpRequest.newBuilder()
                .uri(URI.create(
                        AppConfig.getApiEndpoint("/movimentosIngredientes/movStock?id_ingrediente=" + ingredientId)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(moveStockReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                        System.out.println("Stock movimentado com sucesso: " + resp.body());

                        // Recarregar a lista de ingredientes
                        Platform.runLater(() -> {
                            show(); // Reload all ingredients
                            // Clear search field to show all ingredients
                            if (searchField != null) {
                                searchField.clear();
                            }
                        });

                        // Mostrar mensagem de sucesso
                        Platform.runLater(() -> {
                            String message = quantidade > 0 ? "Stock adicionado com sucesso!"
                                    : "Stock removido com sucesso!";
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Stock atualizado", message);
                        });
                    } else {
                        System.err.println("Erro ao movimentar stock: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao movimentar stock",
                                    "Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao movimentar stock",
                                "Erro: " + ex.getMessage());
                    });
                    return null;
                });
    }
}