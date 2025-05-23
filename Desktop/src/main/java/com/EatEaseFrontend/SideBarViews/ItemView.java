package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.DialogHelper;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.StageManager;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * View para gerenciar e exibir itens do menu
 */
public class ItemView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private final NumberFormat currencyFormatter;

    /**
     * Construtor da view de itens
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public ItemView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-PT"));
    }

    /**
     * Carrega e exibe a lista de itens
     */
    public void show() {
        System.out.println("Carregando lista de itens...");

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando itens do menu...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get items
        HttpRequest getItemsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getItemsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        System.out.println("Itens -> " + resp.body());
                        List<Item> items = new ArrayList<>();
                        try {
                            items = ItemJsonLoader.parseItems(resp.body());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        List<Item> finalItems = items;
                        Platform.runLater(() -> {
                            displayItemsAsCards(finalItems);
                        });

                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar itens");
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
                        alert.setHeaderText("Falha ao carregar itens");
                        alert.setContentText("Erro: " + ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Exibe os itens como cards em um FlowPane
     * 
     * @param items Lista de itens a serem exibidos
     */
    private void displayItemsAsCards(List<Item> items) {
        contentArea.getChildren().clear();

        // Create scroll pane for item cards
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));

        // Use FlowPane for responsive cards layout
        FlowPane itemCards = new FlowPane();
        itemCards.setHgap(20);
        itemCards.setVgap(20);
        itemCards.setPadding(new Insets(20));

        // Add section header
        VBox contentBox = new VBox(20);
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label headerLabel = new Label("Itens do Menu");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerBox.getChildren().add(headerLabel);

        // Adicionar botão "Adicionar"
        Button addButton = new Button("Adicionar");
        addButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLUS));
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> showAddItemDialog());

        // Create a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, addButton);

        contentBox.getChildren().add(headerBox);

        // Add item cards
        if (items.isEmpty()) {
            Label noItemsLabel = new Label("Nenhum item encontrado");
            noItemsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noItemsLabel);
        } else {
            for (Item item : items) {
                VBox card = createItemCard(item);
                itemCards.getChildren().add(card);
            }
            contentBox.getChildren().add(itemCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Cria um card para um item
     * 
     * @param item Item para o qual criar o card
     * @return VBox contendo o card do item
     */
    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(300);
        // Aumentamos a altura para acomodar os ingredientes que agora são exibidos por
        // padrão
        card.setPrefHeight(280);
        card.setPadding(new Insets(15));

        // Item name as card title
        Label nameLabel = new Label(item.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.getStyleClass().add("card-title");

        // Item information
        Label idLabel = new Label("ID: " + item.getId());
        Label tipoLabel = new Label("Tipo: " + item.getTipoPratoName());
        Label precoLabel = new Label("Preço: " + currencyFormatter.format(item.getPreco()));

        // Stock information with visual indicator
        HBox stockBox = new HBox(10);
        stockBox.setAlignment(Pos.CENTER_LEFT);

        Label stockLabel = new Label("Stock: " + item.getStockAtual());

        // Add visual indicator for stock level (using a simple heuristic)
        Region stockIndicator = new Region();
        stockIndicator.setPrefWidth(15);
        stockIndicator.setPrefHeight(15);

        // Set color based on stock level (assume below 5 is critical)
        Color indicatorColor;
        if (item.getStockAtual() <= 5) {
            // Critical - Red
            indicatorColor = Color.RED;
        } else if (item.getStockAtual() <= 10) {
            // Low - Orange
            indicatorColor = Color.ORANGE;
        } else {
            // Good - Green
            indicatorColor = Color.GREEN;
        }

        stockIndicator.setBackground(new Background(new BackgroundFill(
                indicatorColor, new CornerRadii(7.5), Insets.EMPTY)));

        stockBox.getChildren().addAll(stockIndicator, stockLabel);

        // Composition info and ingredients - Show ingredients directly in the card
        VBox mainIngredientsContainer = new VBox(5);
        mainIngredientsContainer.setPadding(new Insets(5, 0, 5, 0));

        if (item.getIngredientes() != null && !item.getIngredientes().isEmpty()) {
            // Label com total de ingredientes
            Label composicaoInfo = new Label("Ingredientes (" + item.getIngredientes().size() + "):");
            composicaoInfo.setFont(Font.font("System", FontWeight.BOLD, 12));
            mainIngredientsContainer.getChildren().add(composicaoInfo);

            // Container para a lista de ingredientes com scroll
            VBox ingredientsBox = new VBox(5);
            ingredientsBox.setPadding(new Insets(0, 5, 0, 5));

            // ScrollPane para caso tenha muitos ingredientes - já visível por padrão
            ScrollPane scrollPane = new ScrollPane(ingredientsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setMaxHeight(160); // Altura aumentada para mostrar mais ingredientes
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setStyle("-fx-background-color: transparent;");

            // Buscar ingredientes e mostrar na lista automaticamente
            fetchAndShowIngredientsList(item, ingredientsBox);

            mainIngredientsContainer.getChildren().add(scrollPane);
        } else {
            Label composicaoInfo = new Label("Item simples (sem ingredientes)");
            mainIngredientsContainer.getChildren().add(composicaoInfo);
        }

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
        editButton.setOnAction(e -> showEditItemDialog(item));

        // Delete button
        Button deleteButton = new Button("");
        deleteButton.setTooltip(new Tooltip("Excluir"));
        FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
        deleteIcon.setIconColor(Color.RED);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(e -> confirmDeleteItem(item));

        // View ingredients button (only for composed items) - Keeping for showing
        // extended details
        Button viewIngredientsButton = new Button("");
        viewIngredientsButton.setTooltip(new Tooltip("Ver Detalhes Completos dos Ingredientes"));
        FontIcon ingredientsIcon = new FontIcon(MaterialDesign.MDI_FORMAT_LIST_BULLETED);
        ingredientsIcon.setIconColor(Color.GREEN);
        viewIngredientsButton.setGraphic(ingredientsIcon);
        viewIngredientsButton.getStyleClass().add("icon-button");
        viewIngredientsButton.setOnAction(e -> showItemIngredients(item));
        viewIngredientsButton.setDisable(!item.isEComposto());

        buttonsBox.getChildren().addAll(viewIngredientsButton, editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, idLabel, tipoLabel, precoLabel, stockBox, mainIngredientsContainer,
                buttonsBox);

        return card;
    }

    /**
     * Exibe o diálogo para adicionar um novo item
     */
    private void showAddItemDialog() {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Item ao Menu");
        dialog.setHeaderText("Preencha as informações do novo item");

        // Configurar o diálogo para usar o primaryStage como owner e não ser
        // redimensionável
        DialogHelper.configureDialog(dialog);

        // Configurar o dialog para usar o primaryStage como owner e evitar
        // redimensionamento
        StageManager.setupDialog(dialog);

        // Botões do diálogo
        ButtonType addButtonType = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Criação dos campos de entrada
        TextField nameField = new TextField();
        nameField.setPromptText("Nome do item");

        ComboBox<String> tipoPratoComboBox = new ComboBox<>();
        tipoPratoComboBox.getItems().addAll("Entrada", "Prato Principal", "Sobremesa", "Bebida");
        tipoPratoComboBox.setPromptText("Selecione o tipo de prato");

        TextField precoField = new TextField();
        precoField.setPromptText("Preço (exemplo: 12.99)");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock atual");

        CheckBox compostoCheckBox = new CheckBox("Item composto por ingredientes");

        // Layout do diálogo - área principal
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Adicionar campos ao grid
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Tipo de Prato:"), 0, 1);
        grid.add(tipoPratoComboBox, 1, 1);
        grid.add(new Label("Preço:"), 0, 2);
        grid.add(precoField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(compostoCheckBox, 0, 4, 2, 1);

        // Área de ingredientes
        TitledPane ingredientesPane = new TitledPane();
        ingredientesPane.setText("Ingredientes");
        ingredientesPane.setExpanded(false);
        ingredientesPane.setDisable(true);

        VBox ingredientesBox = new VBox(10);
        ingredientesBox.setPadding(new Insets(10));

        // Tabela de ingredientes selecionados
        TableView<IngredientRowData> ingredientsTable = new TableView<>();
        ingredientsTable.setPrefHeight(200);

        TableColumn<IngredientRowData, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idColumn.setPrefWidth(50);

        TableColumn<IngredientRowData, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(150);

        TableColumn<IngredientRowData, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity())
                        .asObject());
        quantityColumn.setPrefWidth(100);
        quantityColumn.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Integer>() {
                private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
                {
                    spinner.setEditable(true);
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            ((IngredientRowData) getTableRow().getItem()).setQuantity(newValue);
                        }
                    });
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        spinner.getValueFactory().setValue(item);
                        setGraphic(spinner);
                    }
                }
            };
        });

        TableColumn<IngredientRowData, Void> actionColumn = new TableColumn<>("Ações");
        actionColumn.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Void>() {
                private final Button deleteButton = new Button("");
                {
                    FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
                    deleteIcon.setIconColor(Color.RED);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.getStyleClass().add("icon-button");
                    deleteButton.setOnAction(e -> {
                        ingredientsTable.getItems().remove(getTableRow().getIndex());
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            };
        });

        ingredientsTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, actionColumn);

        // Adicionar controles para selecionar ingredientes
        HBox ingredientSelectionBox = new HBox(10);
        ComboBox<Ingredient> ingredientComboBox = new ComboBox<>();
        ingredientComboBox.setPrefWidth(250);
        Button addIngredientButton = new Button("Adicionar");
        addIngredientButton.setDisable(true);

        // Configurar combobox para exibir nomes de ingredientes
        ingredientComboBox.setCellFactory(lv -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome() + " (ID: " + item.getId() + ")");
                }
            }
        });

        ingredientComboBox.setButtonCell(new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome() + " (ID: " + item.getId() + ")");
                }
            }
        });

        // Adicionar listener para habilitar o botão quando um ingrediente for
        // selecionado
        ingredientComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> addIngredientButton.setDisable(newVal == null));

        // Configurar botão para adicionar ingredientes à tabela
        addIngredientButton.setOnAction(e -> {
            Ingredient selected = ingredientComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Verificar se já existe na tabela
                boolean exists = ingredientsTable.getItems().stream()
                        .anyMatch(item -> item.getId() == selected.getId());

                if (!exists) {
                    ingredientsTable.getItems().add(
                            new IngredientRowData(selected.getId(), selected.getNome(), 1));
                } else {
                    // Mostrar alerta de ingrediente já adicionado
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ingrediente Duplicado");
                    alert.setHeaderText("Ingrediente já adicionado");
                    alert.setContentText("Este ingrediente já foi adicionado à lista.");
                    alert.showAndWait();
                }
            }
        });

        // Botão para carregar ingredientes
        Button loadIngredientsButton = new Button("Carregar Lista de Ingredientes");
        loadIngredientsButton.setPrefWidth(200);

        ingredientSelectionBox.getChildren().addAll(ingredientComboBox, addIngredientButton);
        ingredientesBox.getChildren().addAll(loadIngredientsButton, ingredientSelectionBox, ingredientsTable);
        ingredientesPane.setContent(ingredientesBox);

        // Carregar lista de ingredientes quando o botão for clicado
        loadIngredientsButton.setOnAction(e -> {
            loadIngredientsButton.setDisable(true);
            loadIngredientsButton.setText("Carregando...");

            // Fazer requisição à API
            HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                    .GET()
                    .build();

            httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            List<Ingredient> ingredients = JsonParser.parseIngredients(resp.body());

                            Platform.runLater(() -> {
                                ingredientComboBox.getItems().clear();
                                ingredientComboBox.getItems().addAll(ingredients);
                                loadIngredientsButton.setText("Recarregar Ingredientes");
                                loadIngredientsButton.setDisable(false);
                            });
                        } else {
                            Platform.runLater(() -> {
                                loadIngredientsButton.setText("Falha ao Carregar");
                                loadIngredientsButton.setDisable(false);

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
                            loadIngredientsButton.setText("Falha ao Carregar");
                            loadIngredientsButton.setDisable(false);

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingredientes");
                            alert.setContentText("Erro: " + ex.getMessage());
                            alert.showAndWait();
                        });
                        return null;
                    });
        });

        // Ativar/desativar o painel de ingredientes com base no checkbox
        compostoCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ingredientesPane.setDisable(!newVal);
            ingredientesPane.setExpanded(newVal);
        });

        // Adicionar todos os componentes em um container principal
        VBox mainContainer = new VBox(20);
        mainContainer.getChildren().addAll(grid, ingredientesPane);
        dialog.getDialogPane().setContent(mainContainer);

        // Configurar validação de campos
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Validador para verificar se todos os campos obrigatórios estão preenchidos
        Runnable validateFields = () -> {
            boolean valid = !nameField.getText().trim().isEmpty() &&
                    tipoPratoComboBox.getValue() != null &&
                    !precoField.getText().trim().isEmpty() &&
                    isNumeric(precoField.getText().trim()) &&
                    !stockField.getText().trim().isEmpty() &&
                    isNumeric(stockField.getText().trim()) &&
                    (!compostoCheckBox.isSelected() ||
                            (compostoCheckBox.isSelected() && !ingredientsTable.getItems().isEmpty()));

            addButton.setDisable(!valid);
        };

        // Adicionar listeners para todos os campos
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        tipoPratoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        precoField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        stockField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        compostoCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        ingredientsTable.getItems().addListener((javafx.collections.ListChangeListener<IngredientRowData>) change -> {
            validateFields.run();
        });

        // Processar o resultado quando o botão Adicionar for clicado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    // Validar e converter valores
                    String nome = nameField.getText().trim();
                    int tipoPratoId = tipoPratoComboBox.getSelectionModel().getSelectedIndex() + 1;
                    double preco = Double.parseDouble(precoField.getText().trim().replace(',', '.'));
                    int stock = Integer.parseInt(stockField.getText().trim());
                    boolean composto = compostoCheckBox.isSelected();

                    // Criar a lista de ingredientes
                    List<Item.ItemIngrediente> ingredientes = new ArrayList<>();
                    if (composto) {
                        // Serializar ingredientes
                        for (IngredientRowData row : ingredientsTable.getItems()) {
                            Item.ItemIngrediente ingrediente = new Item.ItemIngrediente();
                            ingrediente.setIngredienteId(row.getId());
                            ingrediente.setQuantidade(row.getQuantity());
                            ingredientes.add(ingrediente);
                        }
                    }

                    // Criar e enviar o item para a API
                    createItem(nome, tipoPratoId, preco, composto, stock, ingredientes);

                    return null;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro de Validação");
                    alert.setHeaderText("Valores Inválidos");
                    alert.setContentText("Por favor, verifique os campos numéricos (preço e stock).");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        // Definir o tamanho do diálogo
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(600);

        // Mostrar o diálogo e esperar pelo resultado
        dialog.showAndWait();
    }

    /**
     * Envia uma requisição para a API para criar um novo item
     *
     * @param nome         Nome do item
     * @param tipoPratoId  ID do tipo de prato
     * @param preco        Preço do item
     * @param composto     Indica se o item é composto de ingredientes
     * @param stock        Quantidade em stock
     * @param ingredientes Lista de ingredientes do item (apenas para itens
     *                     compostos)
     */
    private void createItem(String nome, int tipoPratoId, double preco, boolean composto,
            int stock, List<Item.ItemIngrediente> ingredientes) {
        // Mostrar indicador de progresso

        try {
            // Construir o JSON do item
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"nome\":\"").append(nome).append("\",");
            jsonBuilder.append("\"tipoPratoId\":").append(tipoPratoId).append(",");
            jsonBuilder.append("\"preco\":").append(preco).append(",");
            // Corrigido para usar "composto" ao invés de "eComposto"
            jsonBuilder.append("\"composto\":").append(composto).append(",");
            jsonBuilder.append("\"stockAtual\":").append(stock);

            // Adicionar ingredientes se o item for composto
            if (composto && ingredientes != null && !ingredientes.isEmpty()) {
                jsonBuilder.append(",\"ingredientes\":[");

                for (int i = 0; i < ingredientes.size(); i++) {
                    Item.ItemIngrediente ingrediente = ingredientes.get(i);
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"ingredienteId\":").append(ingrediente.getIngredienteId()).append(",");
                    jsonBuilder.append("\"quantidade\":").append(ingrediente.getQuantidade());
                    jsonBuilder.append("}");

                    if (i < ingredientes.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }

                jsonBuilder.append("]");
            }

            jsonBuilder.append("}");
            String jsonBody = jsonBuilder.toString();

            System.out.println("JSON enviado: " + jsonBody);

            // Criar a requisição HTTP
            HttpRequest createItemReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/item/create")))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Enviar a requisição de forma assíncrona
            httpClient.sendAsync(createItemReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {

                        Platform.runLater(() -> {
                            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                                // Sucesso
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("Sucesso");
                                successAlert.setHeaderText("Item Criado");
                                successAlert.setContentText("O item foi criado com sucesso!");

                                successAlert.showAndWait();

                                // Recarregar a lista de itens
                                show();
                            } else {
                                // Erro
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Erro");
                                errorAlert.setHeaderText("Falha ao criar item");
                                errorAlert.setContentText(
                                        "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());

                                errorAlert.showAndWait();
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();

                        Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText("Falha ao criar item");
                            errorAlert.setContentText("Ocorreu um erro ao tentar enviar os dados: " + ex.getMessage());

                            errorAlert.showAndWait();
                        });

                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText("Falha ao criar item");
                errorAlert.setContentText("Ocorreu um erro ao processar os dados: " + e.getMessage());

                errorAlert.showAndWait();
            });
        }
    }

    /**
     * Classe auxiliar para representar uma linha na tabela de ingredientes
     */
    private static class IngredientRowData {
        private final int id;
        private final String name;
        private int quantity;

        public IngredientRowData(int id, String name, int quantity) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * Verifica se uma string é numérica
     * 
     * @param str String a ser verificada
     * @return true se a string for numérica, false caso contrário
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.replace(',', '.');
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Exibe o diálogo para editar um item existente
     * 
     * @param item Item a ser editado
     */
    private void showEditItemDialog(Item item) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Editar Item");
        dialog.setHeaderText("Editar informações do item: " + item.getNome());

        // Botões do diálogo
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Criação dos campos de entrada com valores pré-preenchidos
        TextField nameField = new TextField(item.getNome());

        ComboBox<String> tipoPratoComboBox = new ComboBox<>();
        tipoPratoComboBox.getItems().addAll("Entrada", "Prato Principal", "Sobremesa", "Bebida");
        tipoPratoComboBox.getSelectionModel().select(item.getTipoPratoId() - 1);

        TextField precoField = new TextField(String.format("%.2f", item.getPreco()).replace('.', ','));

        TextField stockField = new TextField(String.valueOf(item.getStockAtual()));

        CheckBox compostoCheckBox = new CheckBox("Item composto por ingredientes");
        compostoCheckBox.setSelected(item.isEComposto());

        // Layout do diálogo - área principal
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Adicionar campos ao grid
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Tipo de Prato:"), 0, 1);
        grid.add(tipoPratoComboBox, 1, 1);
        grid.add(new Label("Preço:"), 0, 2);
        grid.add(precoField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(compostoCheckBox, 0, 4, 2, 1);

        // Área de ingredientes
        TitledPane ingredientesPane = new TitledPane();
        ingredientesPane.setText("Ingredientes");
        ingredientesPane.setExpanded(item.isEComposto());
        ingredientesPane.setDisable(!item.isEComposto());

        VBox ingredientesBox = new VBox(10);
        ingredientesBox.setPadding(new Insets(10));

        // Tabela de ingredientes selecionados
        TableView<IngredientRowData> ingredientsTable = new TableView<>();
        ingredientsTable.setPrefHeight(200);

        TableColumn<IngredientRowData, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        idColumn.setPrefWidth(50);

        TableColumn<IngredientRowData, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setPrefWidth(150);

        TableColumn<IngredientRowData, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity())
                        .asObject());
        quantityColumn.setPrefWidth(100);
        quantityColumn.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Integer>() {
                private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
                {
                    spinner.setEditable(true);
                    spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            ((IngredientRowData) getTableRow().getItem()).setQuantity(newValue);
                        }
                    });
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        spinner.getValueFactory().setValue(item);
                        setGraphic(spinner);
                    }
                }
            };
        });

        TableColumn<IngredientRowData, Void> actionColumn = new TableColumn<>("Ações");
        actionColumn.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Void>() {
                private final Button deleteButton = new Button("");
                {
                    FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
                    deleteIcon.setIconColor(Color.RED);
                    deleteButton.setGraphic(deleteIcon);
                    deleteButton.getStyleClass().add("icon-button");
                    deleteButton.setOnAction(e -> {
                        ingredientsTable.getItems().remove(getTableRow().getIndex());
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            };
        });

        ingredientsTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, actionColumn);

        // Adicionar controles para selecionar ingredientes
        HBox ingredientSelectionBox = new HBox(10);
        ComboBox<Ingredient> ingredientComboBox = new ComboBox<>();
        ingredientComboBox.setPrefWidth(250);
        Button addIngredientButton = new Button("Adicionar");
        addIngredientButton.setDisable(true);

        // Configurar combobox para exibir nomes de ingredientes
        ingredientComboBox.setCellFactory(lv -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome() + " (ID: " + item.getId() + ")");
                }
            }
        });

        ingredientComboBox.setButtonCell(new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNome() + " (ID: " + item.getId() + ")");
                }
            }
        });

        // Adicionar listener para habilitar o botão quando um ingrediente for
        // selecionado
        ingredientComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> addIngredientButton.setDisable(newVal == null));

        // Configurar botão para adicionar ingredientes à tabela
        addIngredientButton.setOnAction(e -> {
            Ingredient selected = ingredientComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Verificar se já existe na tabela
                boolean exists = ingredientsTable.getItems().stream()
                        .anyMatch(row -> row.getId() == selected.getId());

                if (!exists) {
                    ingredientsTable.getItems().add(
                            new IngredientRowData(selected.getId(), selected.getNome(), 1));
                } else {
                    // Mostrar alerta de ingrediente já adicionado
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ingrediente Duplicado");
                    alert.setHeaderText("Ingrediente já adicionado");
                    alert.setContentText("Este ingrediente já foi adicionado à lista.");
                    alert.showAndWait();
                }
            }
        });

        // Botão para carregar ingredientes
        Button loadIngredientsButton = new Button("Carregar Lista de Ingredientes");
        loadIngredientsButton.setPrefWidth(200);

        // Configurar o botão para recarregar ingredientes quando clicado
        loadIngredientsButton.setOnAction(e -> {
            loadIngredientsButton.setDisable(true);
            loadIngredientsButton.setText("Carregando...");

            // Fazer requisição à API
            HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                    .GET()
                    .build();

            httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            List<Ingredient> ingredients = JsonParser.parseIngredients(resp.body());

                            Platform.runLater(() -> {
                                ingredientComboBox.getItems().clear();
                                ingredientComboBox.getItems().addAll(ingredients);
                                loadIngredientsButton.setText("Recarregar Ingredientes");
                                loadIngredientsButton.setDisable(false);
                            });
                        } else {
                            Platform.runLater(() -> {
                                loadIngredientsButton.setText("Falha ao Carregar");
                                loadIngredientsButton.setDisable(false);

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
                            loadIngredientsButton.setText("Falha ao Carregar");
                            loadIngredientsButton.setDisable(false);

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingredientes");
                            alert.setContentText("Erro: " + ex.getMessage());
                            alert.showAndWait();
                        });
                        return null;
                    });
        });

        ingredientSelectionBox.getChildren().addAll(ingredientComboBox, addIngredientButton);
        ingredientesBox.getChildren().addAll(loadIngredientsButton, ingredientSelectionBox, ingredientsTable);
        ingredientesPane.setContent(ingredientesBox);

        // Carregar a lista de ingredientes automaticamente quando o diálogo é aberto
        Platform.runLater(() -> {
            loadIngredientsButton.setDisable(true);
            loadIngredientsButton.setText("Carregando...");

            // Fazer requisição à API
            HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                    .GET()
                    .build();

            httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            List<Ingredient> ingredients = JsonParser.parseIngredients(resp.body());

                            Platform.runLater(() -> {
                                try {
                                    // Primeiro, preencher o ComboBox com todos os ingredientes disponíveis
                                    ingredientComboBox.getItems().clear();
                                    ingredientComboBox.getItems().addAll(ingredients);

                                    // Se o item sendo editado é composto e tem ingredientes, carregá-los na tabela
                                    if (item.getIngredientes() != null
                                            && !item.getIngredientes().isEmpty()) {
                                        System.out.println("Carregando " + item.getIngredientes().size()
                                                + " ingredientes para o item " + item.getNome());

                                        // Converter os ingredientes do item para IngredientRowData
                                        List<IngredientRowData> ingredientRows = new ArrayList<>();
                                        for (Item.ItemIngrediente itemIng : item.getIngredientes()) {
                                            int ingredientId = itemIng.getIngredienteId();
                                            // Encontrar o nome do ingrediente
                                            String nome = ingredients.stream()
                                                    .filter(ing -> ing.getId() == ingredientId)
                                                    .map(Ingredient::getNome)
                                                    .findFirst()
                                                    .orElse("Desconhecido (ID: " + ingredientId + ")");

                                            System.out.println("Adicionando ingrediente: " + nome + " (ID: "
                                                    + ingredientId + ") - Quantidade: " + itemIng.getQuantidade());

                                            ingredientRows.add(new IngredientRowData(
                                                    ingredientId,
                                                    nome,
                                                    itemIng.getQuantidade()));
                                        }

                                        // Limpar e adicionar todos os ingredientes do item à tabela
                                        ingredientsTable.getItems().clear();
                                        ingredientsTable.getItems().addAll(ingredientRows);
                                    } else {
                                        System.out.println("Item não possui ingredientes");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Erro ao carregar ingredientes: " + e.getMessage());
                                }

                                loadIngredientsButton.setText("Recarregar Ingredientes");
                                loadIngredientsButton.setDisable(false);
                            });
                        } else {
                            Platform.runLater(() -> {
                                loadIngredientsButton.setText("Falha ao Carregar");
                                loadIngredientsButton.setDisable(false);

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
                            loadIngredientsButton.setText("Falha ao Carregar");
                            loadIngredientsButton.setDisable(false);

                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingredientes");
                            alert.setContentText("Erro: " + ex.getMessage());
                            alert.showAndWait();
                        });
                        return null;
                    });
        });

        // Adicionar tudo em um container principal
        VBox mainContainer = new VBox(20);
        mainContainer.getChildren().addAll(grid, ingredientesPane);
        dialog.getDialogPane().setContent(mainContainer);

        // Ativar/desativar o painel de ingredientes com base no checkbox
        compostoCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ingredientesPane.setDisable(!newVal);
            ingredientesPane.setExpanded(newVal);
        });

        ingredientesPane.setDisable(false);
        ingredientesPane.setExpanded(true);

        // Configurar validação de campos
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);

        // Validador para verificar se todos os campos obrigatórios estão preenchidos
        Runnable validateFields = () -> {
            boolean valid = !nameField.getText().trim().isEmpty() &&
                    tipoPratoComboBox.getValue() != null &&
                    !precoField.getText().trim().isEmpty() &&
                    isNumeric(precoField.getText().trim()) &&
                    !stockField.getText().trim().isEmpty() &&
                    isNumeric(stockField.getText().trim()) &&
                    (!compostoCheckBox.isSelected() ||
                            (compostoCheckBox.isSelected() && !ingredientsTable.getItems().isEmpty()));

            saveButton.setDisable(!valid);
        };

        // Adicionar listeners para todos os campos
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        tipoPratoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        precoField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        stockField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        compostoCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        ingredientsTable.getItems().addListener((javafx.collections.ListChangeListener<IngredientRowData>) change -> {
            validateFields.run();
        });

        // Verificar os campos inicialmente
        validateFields.run();

        // Processar o resultado quando o botão Salvar for clicado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validar e converter valores
                    String nome = nameField.getText().trim();
                    int tipoPratoId = tipoPratoComboBox.getSelectionModel().getSelectedIndex() + 1;
                    double preco = Double.parseDouble(precoField.getText().trim().replace(',', '.'));
                    int stock = Integer.parseInt(stockField.getText().trim());
                    boolean composto = compostoCheckBox.isSelected();

                    // Criar a lista de ingredientes
                    List<Item.ItemIngrediente> ingredientes = new ArrayList<>();
                    if (composto) {
                        // Serializar ingredientes
                        for (IngredientRowData row : ingredientsTable.getItems()) {
                            Item.ItemIngrediente ingrediente = new Item.ItemIngrediente();
                            ingrediente.setIngredienteId(row.getId());
                            ingrediente.setQuantidade(row.getQuantity());
                            ingredientes.add(ingrediente);
                        }
                    }

                    // Atualizar o item na API
                    updateItem(item.getId(), nome, tipoPratoId, preco, composto, stock, ingredientes);

                    return null;
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro de Validação");
                    alert.setHeaderText("Valores Inválidos");
                    alert.setContentText("Por favor, verifique os campos numéricos (preço e stock).");
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });

        // Definir o tamanho do diálogo
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(600);

        // Mostrar o diálogo e esperar pelo resultado
        dialog.showAndWait();
    }

    /**
     * Envia uma requisição para a API para atualizar um item existente
     *
     * @param id           ID do item a ser atualizado
     * @param nome         Nome do item
     * @param tipoPratoId  ID do tipo de prato
     * @param preco        Preço do item
     * @param composto     Indica se o item é composto de ingredientes
     * @param stock        Quantidade em stock
     * @param ingredientes Lista de ingredientes do item (apenas para itens
     *                     compostos)
     */
    private void updateItem(int id, String nome, int tipoPratoId, double preco, boolean composto,
            int stock, List<Item.ItemIngrediente> ingredientes) {
        try {
            // Construir o JSON do item
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            // ID é passado como query parameter na URL, não no corpo
            // jsonBuilder.append("\"id\":").append(id).append(",");
            jsonBuilder.append("\"nome\":\"").append(nome).append("\",");
            jsonBuilder.append("\"tipoPratoId\":").append(tipoPratoId).append(",");
            jsonBuilder.append("\"preco\":").append(preco).append(",");
            jsonBuilder.append("\"composto\":").append(composto).append(",");
            jsonBuilder.append("\"stockAtual\":").append(stock);

            // Adicionar ingredientes se o item for composto
            if (composto && ingredientes != null && !ingredientes.isEmpty()) {
                jsonBuilder.append(",\"ingredientes\":[");

                for (int i = 0; i < ingredientes.size(); i++) {
                    Item.ItemIngrediente ingrediente = ingredientes.get(i);
                    jsonBuilder.append("{");
                    jsonBuilder.append("\"ingredienteId\":").append(ingrediente.getIngredienteId()).append(",");
                    jsonBuilder.append("\"quantidade\":").append(ingrediente.getQuantidade());
                    jsonBuilder.append("}");

                    if (i < ingredientes.size() - 1) {
                        jsonBuilder.append(",");
                    }
                }

                jsonBuilder.append("]");
            }

            jsonBuilder.append("}");
            String jsonBody = jsonBuilder.toString();

            System.out.println("JSON enviado: " + jsonBody);

            // Criar a requisição HTTP
            HttpRequest updateItemReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/item/edit?id=" + id)))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Enviar a requisição de forma assíncrona
            httpClient.sendAsync(updateItemReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {

                        Platform.runLater(() -> {
                            if (resp.statusCode() == 200) {
                                // Sucesso
                                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                                successAlert.setTitle("Sucesso");
                                successAlert.setHeaderText("Item Atualizado");
                                successAlert.setContentText("O item foi atualizado com sucesso!");

                                successAlert.showAndWait();

                                // Recarregar a lista de itens
                                show();
                            } else {
                                // Erro
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Erro");
                                errorAlert.setHeaderText("Falha ao atualizar item");
                                errorAlert.setContentText(
                                        "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());

                                errorAlert.showAndWait();
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();

                        Platform.runLater(() -> {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText("Falha ao atualizar item");
                            errorAlert.setContentText("Ocorreu um erro ao tentar enviar os dados: " + ex.getMessage());

                            errorAlert.showAndWait();
                        });

                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erro");
                errorAlert.setHeaderText("Falha ao atualizar item");
                errorAlert.setContentText("Ocorreu um erro ao processar os dados: " + e.getMessage());

                errorAlert.showAndWait();
            });
        }
    }

    /**
     * Exibe diálogo de confirmação para excluir um item
     * 
     * @param item Item a ser excluído
     */
    private void confirmDeleteItem(Item item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Exclusão");
        confirmAlert.setHeaderText("Excluir Item");
        confirmAlert.setContentText("Tem certeza que deseja excluir o item \"" + item.getNome() + "\"?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteItem(item.getId());
        }
    }

    /**
     * Envia uma requisição para a API para excluir um item
     * 
     * @param id ID do item a ser excluído
     */
    private void deleteItem(int id) {
        // Mostrar indicador de progresso

        // Criar a requisição HTTP
        HttpRequest deleteItemReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/delete?id=" + id)))
                .DELETE()
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(deleteItemReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {

                    Platform.runLater(() -> {
                        if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                            // Sucesso
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Sucesso");
                            successAlert.setHeaderText("Item Excluído");
                            successAlert.setContentText("O item foi excluído com sucesso!");

                            successAlert.showAndWait();

                            // Recarregar a lista de itens
                            show();
                        } else {
                            // Erro
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText("Falha ao excluir item");
                            errorAlert.setContentText(
                                    "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());

                            errorAlert.showAndWait();
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erro");
                        errorAlert.setHeaderText("Falha ao excluir item");
                        errorAlert.setContentText("Ocorreu um erro ao tentar enviar a solicitação: " + ex.getMessage());

                        errorAlert.showAndWait();
                    });

                    return null;
                });
    }

    /**
     * Mostra os detalhes dos ingredientes de um item
     * 
     * @param item Item cujos ingredientes serão exibidos
     */
    private void showItemIngredients(Item item) {
        if (!item.isEComposto()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informação");
            alert.setHeaderText("Item Simples");
            alert.setContentText("Este item não é composto, portanto não possui ingredientes associados.");
            alert.showAndWait();
            return;
        }

        if (item.getIngredientes() == null || item.getIngredientes().isEmpty()) {
            // Show dialog with raw JSON for debugging purposes
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Problema de Formato");
            alert.setHeaderText("Problemas ao Processar Ingredientes");

            TextArea textArea = new TextArea();
            textArea.setText("Este item deveria ter ingredientes, mas nenhum foi encontrado.\n\n" +
                    "JSON Original: " + item.getIngredientes() + "\n\n" +
                    "Isso pode indicar um problema no formato do JSON recebido da API.");
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(200);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.getDialogPane().setExpanded(true);
            alert.showAndWait();
            return;
        }

        // Criar um diálogo com a lista de ingredientes
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ingredientes do Item");
        dialog.setHeaderText("Ingredientes de " + item.getNome());

        // Criar uma lista de ingredientes
        VBox ingredientsList = new VBox(8);
        ingredientsList.setPadding(new Insets(10));

        // Cabeçalho da lista
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label idHeaderLabel = new Label("ID");
        idHeaderLabel.setMinWidth(50);
        idHeaderLabel.setStyle("-fx-font-weight: bold");

        Label qtyHeaderLabel = new Label("Quantidade");
        qtyHeaderLabel.setStyle("-fx-font-weight: bold");

        header.getChildren().addAll(idHeaderLabel, qtyHeaderLabel);
        ingredientsList.getChildren().add(header);

        // Separador
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        ingredientsList.getChildren().add(separator);

        // Listar ingredientes
        for (Item.ItemIngrediente ingrediente : item.getIngredientes()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label idLabel = new Label(String.valueOf(ingrediente.getIngredienteId()));
            idLabel.setMinWidth(50);

            Label quantidadeLabel = new Label(ingrediente.getQuantidade() + " unidades");

            // Criar botão para carregar detalhes do ingrediente
            Button detailsButton = new Button("");
            detailsButton.setTooltip(new Tooltip("Ver Detalhes"));
            FontIcon infoIcon = new FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE);
            infoIcon.setIconColor(Color.BLUE);
            detailsButton.setGraphic(infoIcon);
            detailsButton.getStyleClass().add("icon-button");
            final int ingredientId = ingrediente.getIngredienteId();
            detailsButton.setOnAction(e -> fetchAndShowIngredientDetails(ingredientId));

            row.getChildren().addAll(idLabel, quantidadeLabel, detailsButton);
            ingredientsList.getChildren().add(row);
        }

        // Adicionar botão para carregar todos os nomes de ingredientes
        Button loadAllNamesButton = new Button("Carregar Nomes de Ingredientes");
        loadAllNamesButton.setOnAction(e -> loadIngredientNames(dialog, item));

        // Adicionar nota informativa
        Label noteLabel = new Label(
                "Nota: Clique no botão de informações para ver detalhes de um ingrediente específico.");
        noteLabel.setWrapText(true);
        noteLabel.setPadding(new Insets(10, 0, 0, 0));
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");

        // Adicionar JSON bruto para diagnóstico (em uma área expandível)
        TitledPane rawJsonPane = new TitledPane();
        rawJsonPane.setText("JSON Original (Diagnóstico)");

        TextArea jsonTextArea = new TextArea(item.getIngredientes().toString());
        jsonTextArea.setEditable(false);
        jsonTextArea.setPrefHeight(100);
        rawJsonPane.setContent(jsonTextArea);
        rawJsonPane.setExpanded(false);

        // Adicionar tudo a um painel de rolagem
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(10);
        content.getChildren().addAll(ingredientsList, loadAllNamesButton, noteLabel, rawJsonPane);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setPrefHeight(300);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(400);

        dialog.showAndWait();
    }

    /**
     * Carrega os nomes de todos os ingredientes para um item
     * 
     * @param parentDialog O diálogo pai onde os resultados serão exibidos
     * @param item         O item cujos ingredientes serão carregados
     */
    private void loadIngredientNames(Dialog<?> parentDialog, Item item) {
        // Mostrar indicador de carregamento
        parentDialog.setHeaderText("Carregando ingredientes de " + item.getNome() + "...");

        // Fazer requisição à API para obter todos os ingredientes
        HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        List<Ingredient> allIngredients = JsonParser.parseIngredients(resp.body());

                        // Criar mapeamento de IDs para nomes de ingredientes
                        StringBuilder info = new StringBuilder();
                        info.append("Ingredientes encontrados:\n\n");

                        for (Item.ItemIngrediente itemIngrediente : item.getIngredientes()) {
                            int id = itemIngrediente.getIngredienteId();
                            int quantidade = itemIngrediente.getQuantidade();

                            // Procurar ingrediente pelo ID
                            String nome = allIngredients.stream()
                                    .filter(ing -> ing.getId() == id)
                                    .map(Ingredient::getNome)
                                    .findFirst()
                                    .orElse("Desconhecido");

                            info.append(nome).append(" (ID: ").append(id).append("): ")
                                    .append(quantidade).append(" unidades\n");
                        }

                        // Atualizar diálogo com as informações
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Ingredientes Completos");
                            alert.setHeaderText("Ingredientes de " + item.getNome());

                            TextArea textArea = new TextArea(info.toString());
                            textArea.setEditable(false);
                            textArea.setWrapText(true);

                            alert.getDialogPane().setContent(textArea);
                            alert.getDialogPane().setPrefWidth(400);
                            alert.getDialogPane().setPrefHeight(300);

                            parentDialog.setHeaderText("Ingredientes de " + item.getNome());
                            alert.showAndWait();
                        });
                    } else {
                        Platform.runLater(() -> {
                            parentDialog.setHeaderText("Ingredientes de " + item.getNome());
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
                        parentDialog.setHeaderText("Ingredientes de " + item.getNome());
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
     * Busca e exibe detalhes de um ingrediente específico
     * 
     * @param ingredientId ID do ingrediente
     */
    private void fetchAndShowIngredientDetails(int ingredientId) {
        // Mostrar indicador de carregamento
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Carregando");
        loadingAlert.setHeaderText("Carregando detalhes do ingrediente...");
        loadingAlert.setContentText("Aguarde enquanto carregamos as informações...");

        // Criar ProgressIndicator para feedback visual
        ProgressIndicator progress = new ProgressIndicator();
        loadingAlert.getDialogPane().setGraphic(progress);

        // Mostrar alerta sem botões (não-modal)
        loadingAlert.getButtonTypes().clear();

        // Mostrar e fechar após um breve intervalo para dar tempo para carregar
        Platform.runLater(() -> {
            loadingAlert.show();

            // Fazer requisição à API para obter o ingrediente
            HttpRequest getIngredientReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/get?id=" + ingredientId)))
                    .GET()
                    .build();

            httpClient.sendAsync(getIngredientReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        loadingAlert.close();

                        if (resp.statusCode() == 200) {
                            try {
                                // Extrair o objeto ingrediente do array JSON
                                String json = resp.body();
                                System.out.println("Ingredient JSON: " + json);

                                // O parseIngredients espera um array, então envolvemos a resposta em colchetes
                                // se necessário
                                if (!json.trim().startsWith("[")) {
                                    json = "[" + json + "]";
                                }

                                List<Ingredient> ingredients = JsonParser.parseIngredients(json);

                                if (!ingredients.isEmpty()) {
                                    Ingredient ingredient = ingredients.get(0);
                                    Platform.runLater(() -> {
                                        showIngredientDetails(ingredient);
                                    });
                                } else {
                                    throw new Exception("Nenhum ingrediente encontrado na resposta");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Erro");
                                    alert.setHeaderText("Erro ao processar dados do ingrediente");
                                    alert.setContentText("Ocorreu um erro ao processar os dados. " + e.getMessage());
                                    alert.showAndWait();
                                });
                            }
                        } else {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Erro");
                                alert.setHeaderText("Falha ao carregar ingrediente");
                                alert.setContentText("Status code: " + resp.statusCode());
                                alert.showAndWait();
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        loadingAlert.close();
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar ingrediente");
                            alert.setContentText("Erro: " + ex.getMessage());
                            alert.showAndWait();
                        });
                        return null;
                    });
        });
    }

    /**
     * Exibe os detalhes de um ingrediente
     * 
     * @param ingredient O ingrediente a ser exibido
     */
    private void showIngredientDetails(Ingredient ingredient) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalhes do Ingrediente");
        alert.setHeaderText(ingredient.getNome());

        // Criar grid para os detalhes
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Adicionar os detalhes ao grid
        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(String.valueOf(ingredient.getId())), 1, 0);

        grid.add(new Label("Nome:"), 0, 1);
        grid.add(new Label(ingredient.getNome()), 1, 1);

        grid.add(new Label("Stock:"), 0, 2);
        grid.add(new Label(String.valueOf(ingredient.getStock())), 1, 2);

        grid.add(new Label("Stock Mínimo:"), 0, 3);
        grid.add(new Label(String.valueOf(ingredient.getStock_min())), 1, 3);

        grid.add(new Label("Unidade:"), 0, 4);
        grid.add(new Label(ingredient.getUnidadeName()), 1, 4);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    /**
     * Busca os detalhes dos ingredientes de um item e os exibe em uma lista
     * 
     * @param item      Item cujos ingredientes serão exibidos
     * @param container Container onde a lista de ingredientes será exibida
     */
    private void fetchAndShowIngredientsList(Item item, VBox container) {
        // Adicionar um label de carregamento temporário
        Label loadingLabel = new Label("Carregando ingredientes...");
        loadingLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        container.getChildren().add(loadingLabel);

        // Fazer requisição à API para obter todos os ingredientes
        HttpRequest getIngredientsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getIngredientsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        List<Ingredient> allIngredients = JsonParser.parseIngredients(resp.body());

                        Platform.runLater(() -> {
                            // Remover o label de carregamento
                            container.getChildren().remove(loadingLabel);

                            // Para cada ingrediente do item, criar uma linha com nome e quantidade
                            for (Item.ItemIngrediente itemIngrediente : item.getIngredientes()) {
                                int id = itemIngrediente.getIngredienteId();
                                int quantidade = itemIngrediente.getQuantidade();

                                // Procurar ingrediente pelo ID
                                String nome = allIngredients.stream()
                                        .filter(ing -> ing.getId() == id)
                                        .map(Ingredient::getNome)
                                        .findFirst()
                                        .orElse("Desconhecido");

                                // Obter unidade do ingrediente para formatar corretamente
                                Ingredient foundIngredient = allIngredients.stream()
                                        .filter(ing -> ing.getId() == id)
                                        .findFirst()
                                        .orElse(null);

                                String formattedQuantity;
                                if (foundIngredient != null) {
                                    int unidadeId = foundIngredient.getUnidade_id();
                                    switch (unidadeId) {
                                        case 1: // quilos
                                            formattedQuantity = quantidade + " kg";
                                            break;
                                        case 2: // gramas
                                            formattedQuantity = quantidade + " g";
                                            break;
                                        case 3: // litros
                                            formattedQuantity = quantidade + " L";
                                            break;
                                        case 4: // mililitros
                                            formattedQuantity = quantidade + " ml";
                                            break;
                                        case 5: // unidades
                                            formattedQuantity = quantidade + " un";
                                            break;
                                        case 6: // doses
                                            formattedQuantity = quantidade + " dose" + (quantidade > 1 ? "s" : "");
                                            break;
                                        case 7: // caixas
                                            formattedQuantity = quantidade + " caixa" + (quantidade > 1 ? "s" : "");
                                            break;
                                        default:
                                            formattedQuantity = quantidade + " un";
                                    }
                                } else {
                                    formattedQuantity = quantidade + " un";
                                }

                                // Criar linha para o ingrediente
                                HBox ingredientRow = new HBox(5);
                                Label ingredientLabel = new Label("• " + nome + ": " + formattedQuantity);
                                ingredientLabel.setStyle("-fx-font-size: 11px;");
                                ingredientRow.getChildren().add(ingredientLabel);
                                container.getChildren().add(ingredientRow);
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            // Em caso de erro, mostrar mensagem adequada
                            container.getChildren().remove(loadingLabel);
                            Label errorLabel = new Label("Erro ao carregar ingredientes");
                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                            container.getChildren().add(errorLabel);
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        // Em caso de erro, mostrar mensagem adequada
                        container.getChildren().remove(loadingLabel);
                        Label errorLabel = new Label("Erro ao carregar ingredientes");
                        errorLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
                        container.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }
}
