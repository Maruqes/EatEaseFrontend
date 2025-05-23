package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.JsonParser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * View para gerenciar e exibir ingredientes
 */
public class IngredientsView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

    /**
     * Construtor da view de ingredientes
     *
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public IngredientsView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
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
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingredientes");
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
                        alert.setHeaderText("Falha ao carregar ingredientes");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
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
        addButton.setOnAction(e -> showAddIngredientDialog());

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
        Label unitLabel = new Label("Unidade ID: " + ingredient.getUnidade_id());

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
        editButton.setOnAction(e -> showEditIngredientDialog(ingredient));

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
    private void showAddIngredientDialog() {
        Dialog<Ingredient> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Ingrediente");
        dialog.setHeaderText("Preencha as informações do novo ingrediente");

        // Criação dos campos de entrada
        TextField nameField = new TextField();
        nameField.setPromptText("Nome do ingrediente");

        TextField stockField = new TextField();
        stockField.setPromptText("Quantidade em stock");

        TextField stockMinField = new TextField();
        stockMinField.setPromptText("Quantidade mínima em stock");

        TextField unidadeMedidaField = new TextField();
        unidadeMedidaField.setPromptText("Unidade de Medida");

        // Layout do diálogo
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Stock:"), 0, 1);
        grid.add(stockField, 1, 1);
        grid.add(new Label("Stock Mínimo:"), 0, 2);
        grid.add(stockMinField, 1, 2);
        grid.add(new Label("Unidade de Medida:"), 0, 3);
        grid.add(unidadeMedidaField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Botões do diálogo
        ButtonType addButtonType = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Habilitar/desabilitar botão de adicionar baseado na validação
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Validar campos à medida que são preenchidos
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            addButton.setDisable(nameField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        stockMinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockMinField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            addButton.setDisable(nameField.getText().trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        unidadeMedidaField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(nameField.getText().trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty());
        });

        // Converter resultado do diálogo para criar um novo ingrediente
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String nome = nameField.getText();
                    int stock = Integer.parseInt(stockField.getText());
                    int stockMinimo = Integer.parseInt(stockMinField.getText());
                    // We don't need to store unidadeMedida here as it's passed directly to
                    // createIngredient

                    // Create and return a new ingredient object
                    return new Ingredient(nome, stock, stockMinimo, 0); // Temporarily use 0 for unidade_id
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Valores inválidos");
                    alert.setContentText("Por favor, insira valores numéricos válidos para stock e stock mínimo.");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        // Exibir o diálogo e processar o resultado
        Optional<Ingredient> result = dialog.showAndWait();
        result.ifPresent(ingredient -> {
            createIngredient(ingredient.getNome(), ingredient.getStock(), ingredient.getStock_min(),
                    unidadeMedidaField.getText());
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
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText("Ingrediente adicionado");
                            alert.setContentText("O ingrediente foi adicionado com sucesso.");
                            alert.showAndWait();
                        });
                    } else {
                        System.err.println("Erro ao criar ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao adicionar ingrediente");
                            alert.setContentText("Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao adicionar ingrediente");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Exibe o diálogo para editar um ingrediente existente
     *
     * @param ingredient Ingrediente a ser editado
     */
    private void showEditIngredientDialog(Ingredient ingredient) {
        Dialog<Ingredient> dialog = new Dialog<>();
        dialog.setTitle("Editar Ingrediente");
        dialog.setHeaderText("Editar informações do ingrediente");

        // Criação dos campos de entrada com valores atuais
        TextField nameField = new TextField(ingredient.getNome());
        nameField.setPromptText("Nome do ingrediente");

        TextField stockField = new TextField(String.valueOf(ingredient.getStock()));
        stockField.setPromptText("Quantidade em stock");

        TextField stockMinField = new TextField(String.valueOf(ingredient.getStock_min()));
        stockMinField.setPromptText("Quantidade mínima em stock");

        TextField unidadeMedidaField = new TextField(String.valueOf(ingredient.getUnidade_id()));
        unidadeMedidaField.setPromptText("Unidade de Medida");

        // Layout do diálogo
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Stock:"), 0, 1);
        grid.add(stockField, 1, 1);
        grid.add(new Label("Stock Mínimo:"), 0, 2);
        grid.add(stockMinField, 1, 2);
        grid.add(new Label("Unidade de Medida:"), 0, 3);
        grid.add(unidadeMedidaField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Botões do diálogo
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Habilitar/desabilitar botão de salvar baseado na validação
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validar campos à medida que são preenchidos
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            saveButton.setDisable(nameField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        stockMinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockMinField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            saveButton.setDisable(nameField.getText().trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty() ||
                    unidadeMedidaField.getText().trim().isEmpty());
        });

        unidadeMedidaField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() ||
                    stockField.getText().trim().isEmpty() ||
                    stockMinField.getText().trim().isEmpty() ||
                    newValue.trim().isEmpty());
        });

        // Converter resultado do diálogo para editar ingrediente
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String nome = nameField.getText();
                    int stock = Integer.parseInt(stockField.getText());
                    int stockMinimo = Integer.parseInt(stockMinField.getText());
                    // We'll get unidadeMedida directly when calling updateIngredient

                    // Update and return ingredient object
                    ingredient.setNome(nome);
                    ingredient.setStock(stock);
                    ingredient.setStock_min(stockMinimo);

                    return ingredient;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Valores inválidos");
                    alert.setContentText("Por favor, insira valores numéricos válidos para stock e stock mínimo.");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        // Exibir o diálogo e processar o resultado
        Optional<Ingredient> result = dialog.showAndWait();
        result.ifPresent(updatedIngredient -> {
            updateIngredient(updatedIngredient.getId(), updatedIngredient.getNome(),
                    updatedIngredient.getStock(), updatedIngredient.getStock_min(),
                    unidadeMedidaField.getText());
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
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText("Ingrediente atualizado");
                            alert.setContentText("O ingrediente foi atualizado com sucesso.");
                            alert.showAndWait();
                        });
                    } else {
                        System.err.println("Erro ao atualizar ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao atualizar ingrediente");
                            alert.setContentText("Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao atualizar ingrediente");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
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
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar Exclusão");
        confirmDialog.setHeaderText("Excluir Ingrediente");
        confirmDialog.setContentText("Tem certeza que deseja excluir o ingrediente '" + ingredient.getNome() + "'?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteIngredient(ingredient.getId());
        }
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
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText("Ingrediente excluído");
                            alert.setContentText("O ingrediente foi excluído com sucesso.");
                            alert.showAndWait();
                        });
                    } else {
                        System.err.println("Erro ao excluir ingrediente: " + resp.statusCode() + " - " + resp.body());

                        // Mostrar mensagem de erro
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao excluir ingrediente");
                            alert.setContentText("Status code: " + resp.statusCode() + "\nResposta: " + resp.body());
                            alert.showAndWait();
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    // Mostrar mensagem de erro
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao excluir ingrediente");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }
}