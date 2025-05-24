package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.DialogHelper;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.StageManager;
import com.EatEaseFrontend.SideBarViews.PopUp;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
import java.util.Optional;

/**
 * View para gerenciar e exibir ingredientes
 */
public class IngredientsView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

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
     * Get the unit ID from its name
     * 
     * @param unidadeName Name of the unit
     * @return ID of the unit or 1 (Quilos) if not found
     */
    private int getUnidadeId(String unidadeName) {
        for (Map.Entry<Integer, String> entry : UNIDADE_MAP.entrySet()) {
            if (entry.getValue().equals(unidadeName)) {
                return entry.getKey();
            }
        }
        return 1; // Default to Quilos if not found
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

                        Platform.runLater(() -> {
                            displayIngredientsAsCards(ingredients);
                        });
                    } else {
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingredientes",
                                    "Status code: " + resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingredientes",
                                "Erro: " + ex.getMessage());
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

        // Adicionar botão "Adicionar"
        Button addButton = new Button("Adicionar");
        addButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLUS));
        addButton.getStyleClass().add("login-button"); // Use the same style as login button for consistency
        addButton.setOnAction(e -> showAddIngredientPopup());

        // Create a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, addButton);

        contentBox.getChildren().add(headerBox);

        // Add ingredient cards
        if (ingredients.isEmpty()) {
            Label noIngredientsLabel = new Label("Nenhum ingrediente encontrado");
            noIngredientsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noIngredientsLabel);
        } else {
            for (Ingredient ingredient : ingredients) {
                VBox card = createIngredientCard(ingredient);
                ingredientCards.getChildren().add(card);
            }
            contentBox.getChildren().add(ingredientCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
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
        deleteButton.setOnAction(e -> confirmDeleteIngredient(ingredient));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, idLabel, stockBox, stockMinLabel, unitLabel, buttonsBox);

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
        popup.setAutoHide(true); // fecha ao clicar fora

        // 3) Campos do formulário
        TextField nameField = new TextField();
        nameField.setPromptText("Nome do ingrediente");

        TextField stockField = new TextField();
        stockField.setPromptText("Quantidade em stock");
        stockField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockField.setText(n.replaceAll("[^\\d]", ""));
        });

        TextField stockMinField = new TextField();
        stockMinField.setPromptText("Stock mínimo");
        stockMinField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockMinField.setText(n.replaceAll("[^\\d]", ""));
        });

        ComboBox<String> unidadeCombo = new ComboBox<>();
        unidadeCombo.getItems().addAll(UNIDADE_NAMES.subList(1, UNIDADE_NAMES.size()));
        unidadeCombo.setPromptText("Unidade de medida");
        unidadeCombo.setPrefWidth(200);

        // 4) Botões e validação
        Button addBtn = new Button("Adicionar");
        Button cancelBtn = new Button("Cancelar");
        addBtn.setDisable(true);

        ChangeListener<String> valida = (obs, o, n) -> {
            boolean ok = !nameField.getText().trim().isEmpty()
                    && !stockField.getText().trim().isEmpty()
                    && !stockMinField.getText().trim().isEmpty()
                    && unidadeCombo.getValue() != null;
            addBtn.setDisable(!ok);
        };
        nameField.textProperty().addListener(valida);
        stockField.textProperty().addListener(valida);
        stockMinField.textProperty().addListener(valida);
        unidadeCombo.valueProperty().addListener((o, ov, nv) -> valida.changed(null, null, null));

        // 5) Layout no GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Stock:"), 0, 1);
        grid.add(stockField, 1, 1);
        grid.add(new Label("Stock Mínimo:"), 0, 2);
        grid.add(stockMinField, 1, 2);
        grid.add(new Label("Unidade:"), 0, 3);
        grid.add(unidadeCombo, 1, 3);
        HBox buttons = new HBox(10, addBtn, cancelBtn);
        grid.add(buttons, 1, 4);

        // 6) Adiciona o grid à popup
        popup.getContent().add(grid);

        // 7) Mostra a popup centrada
        // (ajusta 200x150 se mudares o tamanho do grid)
        popup.show(primary, centerX - 200, centerY - 150);

        // 8) Ações dos botões
        cancelBtn.setOnAction(e -> popup.hide());
        addBtn.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int stock = Integer.parseInt(stockField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            String unidade = unidadeCombo.getValue();
            createIngredient(nome, stock, stockMin, unidade);
            popup.hide();
        });
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
                        Platform.runLater(this::show);

                        // Mostrar mensagem de sucesso
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Ingrediente adicionado!",
                                    "O ingrediente foi criado com sucesso.");
                        });
                    } else {
                        System.err.println("Erro ao criar ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao adicionar ingrediente",
                                    "Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao adicionar ingrediente",
                                "Erro: " + ex.getMessage());
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
        popup.setAutoHide(true); // fecha ao clicar fora

        // 3) Campos do formulário já preenchidos
        TextField nameField = new TextField(ingredient.getNome());
        TextField stockField = new TextField(String.valueOf(ingredient.getStock()));
        TextField stockMinField = new TextField(String.valueOf(ingredient.getStock_min()));

        // Use ComboBox para unidades de medida
        ComboBox<String> unidadeCombo = new ComboBox<>();
        unidadeCombo.getItems().addAll(UNIDADE_NAMES.subList(1, UNIDADE_NAMES.size()));
        unidadeCombo.setValue(getUnidadeName(ingredient.getUnidade_id()));
        unidadeCombo.setPrefWidth(200);

        // só números em stock e stock mínimo
        stockField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockField.setText(n.replaceAll("[^\\d]", ""));
        });
        stockMinField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*"))
                stockMinField.setText(n.replaceAll("[^\\d]", ""));
        });

        // 4) Botões e validação
        Button saveBtn = new Button("Salvar");
        Button cancelBtn = new Button("Cancelar");
        saveBtn.setDisable(true);

        ChangeListener<String> valida = (obs, o, n) -> {
            boolean ok = !nameField.getText().trim().isEmpty()
                    && !stockField.getText().trim().isEmpty()
                    && !stockMinField.getText().trim().isEmpty()
                    && unidadeCombo.getValue() != null;
            saveBtn.setDisable(!ok);
        };
        nameField.textProperty().addListener(valida);
        stockField.textProperty().addListener(valida);
        stockMinField.textProperty().addListener(valida);
        unidadeCombo.valueProperty().addListener((o, ov, nv) -> valida.changed(null, null, null));

        // 5) Layout no GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Stock:"), 0, 1);
        grid.add(stockField, 1, 1);
        grid.add(new Label("Stock Mínimo:"), 0, 2);
        grid.add(stockMinField, 1, 2);
        grid.add(new Label("Unidade de Medida:"), 0, 3);
        grid.add(unidadeCombo, 1, 3);
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        grid.add(buttons, 1, 4);

        // 6) Adiciona o grid à popup
        popup.getContent().add(grid);

        // 7) Mostra a popup centrada (ajusta se necessário)
        popup.show(primary, centerX - 200, centerY - 150);

        // 8) Ações dos botões
        cancelBtn.setOnAction(e -> popup.hide());
        saveBtn.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int stock = Integer.parseInt(stockField.getText());
            int stockMin = Integer.parseInt(stockMinField.getText());
            String unidade = unidadeCombo.getValue();
            // chama o teu método de update
            updateIngredient(ingredient.getId(), nome, stock, stockMin, unidade);
            popup.hide();
        });
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
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(updateIngredientReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                        System.out.println("Ingrediente atualizado com sucesso: " + resp.body());

                        // Recarregar a lista de ingredientes
                        Platform.runLater(this::show);

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
     * Exibe diálogo de confirmação para excluir um ingrediente
     * 
     * @param ingredient Ingrediente a ser excluído
     */
    private void confirmDeleteIngredient(Ingredient ingredient) {
        // Get primary stage for positioning
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(true);

        // Create content
        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");

        // Warning icon
        FontIcon warningIcon = new FontIcon(MaterialDesign.MDI_ALERT);
        warningIcon.setIconSize(32);
        warningIcon.setIconColor(Color.ORANGE);

        // Title and message
        Label titleLabel = new Label("Excluir Ingrediente");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label messageLabel = new Label("Tem certeza que deseja excluir o ingrediente '" + ingredient.getNome() + "'?");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        // Buttons
        Button yesButton = new Button("Sim");
        Button noButton = new Button("Não");

        HBox buttonBox = new HBox(10, yesButton, noButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Add all elements to the popup content
        popupContent.getChildren().addAll(warningIcon, titleLabel, messageLabel, buttonBox);
        popupContent.setAlignment(Pos.CENTER);

        popup.getContent().add(popupContent);

        // Position popup
        popup.show(primary, centerX - 170, centerY - 100);

        // Button actions
        noButton.setOnAction(e -> popup.hide());
        yesButton.setOnAction(e -> {
            deleteIngredient(ingredient.getId());
            popup.hide();
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
                        Platform.runLater(this::show);

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
}