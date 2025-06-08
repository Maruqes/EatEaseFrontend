package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Ingredient;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.StageManager;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.collections.ObservableList;

/**
 * View para gerenciar e exibir itens do menu
 */
public class ItemView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private final NumberFormat currencyFormatter;
    private List<Item> allItems; // Store all items for filtering
    private TextField searchField; // Search field reference

    // Map to store unit IDs and their corresponding names (same as IngredientsView)
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
     * Get the name of the unit from its ID
     * 
     * @param unidadeId ID of the unit
     * @return Name of the unit or "Desconhecido" if not found
     */
    private String getUnidadeName(int unidadeId) {
        if (unidadeId >= 1 && unidadeId < UNIDADE_NAMES.size()) {
            return UNIDADE_NAMES.get(unidadeId);
        }
        return "Desconhecido";
    }

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
                            // Debug: print each item and its composition status
                            for (Item item : items) {
                                System.out.println("DEBUG: Loaded item - " + item.getNome() +
                                        " | isEComposto: " + item.isEComposto() +
                                        " | ingredients count: "
                                        + (item.getIngredientes() != null ? item.getIngredientes().size() : 0));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        allItems = items; // Store all items for filtering
                        List<Item> finalItems = items;
                        Platform.runLater(() -> {
                            displayItemsAsCards(finalItems);
                        });

                    } else {
                        Platform.runLater(() -> {
                            PopUp.showItemLoadError(resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        PopUp.showItemLoadError(ex.getMessage());
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

        // Create search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 20, 0));

        searchField = new TextField();
        searchField.setPromptText("Pesquisar itens...");
        searchField.setPrefWidth(300);
        searchField.setMaxWidth(300);

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterItems(newValue);
        });

        searchBox.getChildren().add(searchField);

        // Adicionar botão "Adicionar"
        Button addButton = new Button("Adicionar");
        addButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLUS));
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> showAddItemPopup());

        // Create a spacer to push the button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, addButton);

        contentBox.getChildren().addAll(headerBox, searchBox);

        // Add item cards
        if (items.isEmpty()) {
            Label noItemsLabel = new Label("Nenhum item encontrado");
            noItemsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noItemsLabel);
        } else {
            // Sort items alphabetically by name
            List<Item> sortedItems = items.stream()
                    .sorted(Comparator.comparing(Item::getNome, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());

            for (Item item : sortedItems) {
                VBox card = createItemCard(item);
                itemCards.getChildren().add(card);
            }
            contentBox.getChildren().add(itemCards);
        }

        scrollPane.setContent(contentBox);
        contentArea.getChildren().add(scrollPane);
    }

    /**
     * Filters the items based on the search query
     * 
     * @param searchQuery The text to search for in item names
     */
    private void filterItems(String searchQuery) {
        if (allItems == null) {
            return;
        }

        List<Item> filteredItems;

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // Show all items if search is empty
            filteredItems = allItems;
        } else {
            // Filter items that contain the search query (case insensitive)
            String lowerCaseQuery = searchQuery.toLowerCase().trim();
            filteredItems = allItems.stream()
                    .filter(item -> item.getNome().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        // Sort filtered items alphabetically by name
        filteredItems = filteredItems.stream()
                .sorted(Comparator.comparing(Item::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Update the display with filtered items
        displayFilteredItems(filteredItems);
    }

    /**
     * Displays the filtered items without affecting the search bar
     * 
     * @param items List of filtered items to display
     */
    private void displayFilteredItems(List<Item> items) {
        // Find the content box and update only the item cards part
        ScrollPane scrollPane = (ScrollPane) contentArea.getChildren().get(0);
        VBox contentBox = (VBox) scrollPane.getContent();

        // Remove existing item cards (keep header and search bar)
        if (contentBox.getChildren().size() > 2) {
            contentBox.getChildren().remove(2, contentBox.getChildren().size());
        }

        // Create new FlowPane for item cards
        FlowPane itemCards = new FlowPane();
        itemCards.setHgap(20);
        itemCards.setVgap(20);
        itemCards.setPadding(new Insets(20));

        // Add item cards
        if (items.isEmpty()) {
            Label noItemsLabel = new Label("Nenhum item encontrado");
            noItemsLabel.setFont(Font.font("System", FontWeight.NORMAL, 18));
            contentBox.getChildren().add(noItemsLabel);
        } else {
            // Sort items alphabetically by name
            List<Item> sortedItems = items.stream()
                    .sorted(Comparator.comparing(Item::getNome, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());

            for (Item item : sortedItems) {
                VBox card = createItemCard(item);
                itemCards.getChildren().add(card);
            }
            contentBox.getChildren().add(itemCards);
        }
    }

    private void uploadFotoFunc(Item item) {
        // 1. Abre diálogo de ficheiro sem ownerWindow
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolhe a foto para o item " + item.getNome());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return; // utilizador cancelou

        try {
            // 2. Prepara multipart body
            String boundary = "----EatEaseBoundary" + System.currentTimeMillis();
            Path path = file.toPath();
            String mimeType = Files.probeContentType(path);
            if (mimeType == null)
                mimeType = "application/octet-stream";

            List<byte[]> byteArrays = new ArrayList<>();
            String partHeader = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                    "Content-Type: " + mimeType + "\r\n\r\n";
            byteArrays.add(partHeader.getBytes());
            byteArrays.add(Files.readAllBytes(path));
            byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes());

            var bodyPublisher = HttpRequest.BodyPublishers.ofByteArrays(byteArrays);

            // 3. Cria e envia a request
            String url = AppConfig.getApiEndpoint("/item/setFoto?itemId=" + item.getId());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(bodyPublisher)
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        Platform.runLater(() -> {
                            if (resp.statusCode() == 200) {
                                PopUp.showPopupDialog(
                                        Alert.AlertType.INFORMATION,
                                        "Sucesso", "Foto Carregada",
                                        "A foto foi enviada com sucesso para o item “" + item.getNome() + "”.");
                                show(); // recarrega a lista ou detalhe
                            } else {
                                PopUp.showPopupDialog(
                                        Alert.AlertType.ERROR,
                                        "Erro", "Falha no Upload",
                                        "Status: " + resp.statusCode() +
                                                "\nResposta: " + resp.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(
                                    Alert.AlertType.ERROR,
                                    "Erro", "Exceção no Upload",
                                    ex.getMessage());
                        });
                        return null;
                    });

        } catch (Exception e) {
            Platform.runLater(() -> {
                PopUp.showPopupDialog(
                        Alert.AlertType.ERROR,
                        "Erro", "Não foi possível ler o ficheiro",
                        e.getMessage());
            });
        }
    }

    private void seeUploadedFoto(Item item) {
        String filename = item.getFoto();
        String url = AppConfig.getApiEndpoint("/uploads/items/" + filename);

        Platform.runLater(() -> {
            // 1. Pega no primaryStage para posicionar o popup ao centro
            Stage primary = StageManager.getPrimaryStage();

            // 2. Calcular o centro exato da aplicação
            double centerX = primary.getX() + (primary.getWidth() / 2);
            double centerY = primary.getY() + (primary.getHeight() / 2);

            // 3. Definir tamanhos responsivos baseados no tamanho da aplicação
            double appWidth = primary.getWidth();
            double appHeight = primary.getHeight();

            // Popup será 70% da largura e 80% da altura da aplicação, com limites mínimos e
            // máximos
            double popupWidth = Math.max(400, Math.min(800, appWidth * 0.7));
            double popupHeight = Math.max(300, Math.min(600, appHeight * 0.8));

            // 4. Cria o Popup
            Popup popup = new Popup();
            popup.setAutoHide(true);

            // 5. Preparar ImageView com tamanhos responsivos
            Image img = new Image(url, true);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);

            // Tamanho da imagem será 90% do popup menos padding
            double maxImageWidth = popupWidth * 0.9 - 40; // 40 para padding total
            double maxImageHeight = popupHeight * 0.85 - 80; // 80 para título e botão

            iv.setFitWidth(maxImageWidth);
            iv.setFitHeight(maxImageHeight);

            // 6. Container para a imagem com estilo moderno
            VBox imageContainer = new VBox(15);
            imageContainer.setPadding(new Insets(30));
            imageContainer.getStyleClass().add("popup-container");
            imageContainer.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 12;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);" +
                            "-fx-border-radius: 12;");
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setPrefWidth(popupWidth);
            imageContainer.setPrefHeight(popupHeight);

            // 7. Título estilizado
            Label titleLabel = new Label("Foto de " + item.getNome());
            titleLabel.getStyleClass().add("popup-title");
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FB8C00;");

            // 8. Botão para fechar estilizado
            Button closeButton = new Button("Fechar");
            closeButton.getStyleClass().add("popup-primary-button");
            closeButton.setOnAction(e -> popup.hide());

            imageContainer.getChildren().addAll(titleLabel, iv, closeButton);
            popup.getContent().add(imageContainer);

            // 9. Calcular posição para centralizar o popup
            double popupX = centerX - (popupWidth / 2);
            double popupY = centerY - (popupHeight / 2);

            // 10. Mostra o popup centrado
            popup.show(primary, popupX, popupY);
        });
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

        // STOCK COLOR
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
        editButton.setOnAction(e -> showEditItemPopup(item));

        // Delete button
        Button deleteButton = new Button("");
        deleteButton.setTooltip(new Tooltip("Excluir"));
        FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
        deleteIcon.setIconColor(Color.RED);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(e -> confirmDeleteItem(item));

        // upload foto button
        Button uploadFotoButton = new Button("");
        uploadFotoButton.setTooltip(new Tooltip("Carregar Foto do Item"));
        FontIcon uploadIcon = new FontIcon(MaterialDesign.MDI_UPLOAD);
        if (item.getFoto() != null && !item.getFoto().isEmpty()) {
            uploadIcon.setIconColor(Color.GREEN);
        } else {
            uploadIcon.setIconColor(Color.GRAY);
        }
        uploadFotoButton.setGraphic(uploadIcon);
        uploadFotoButton.getStyleClass().add("icon-button");
        uploadFotoButton.setOnAction(e -> {
            uploadFotoFunc(item);
        });

        Button seeUploadedFotoButton = new Button("");
        seeUploadedFotoButton.setTooltip(new Tooltip("Ver Foto do Item"));
        FontIcon seeFotoIcon = new FontIcon(MaterialDesign.MDI_IMAGE);
        seeFotoIcon.setIconColor(Color.BLUE);
        seeUploadedFotoButton.setGraphic(seeFotoIcon);
        seeUploadedFotoButton.getStyleClass().add("icon-button");
        seeUploadedFotoButton.setOnAction(e -> {
            seeUploadedFoto(item);
        });

        buttonsBox.getChildren().addAll(editButton, deleteButton, uploadFotoButton);

        // Add all elements to card
        card.getChildren().addAll(nameLabel, idLabel, tipoLabel, precoLabel, stockBox, mainIngredientsContainer,
                buttonsBox);

        if (item.getFoto() != null && !item.getFoto().isEmpty()) {
            buttonsBox.getChildren().add(seeUploadedFotoButton);
        } else {
            seeUploadedFotoButton.setDisable(true);
        }

        return card;
    }

    /**
     * Exibe o diálogo para adicionar um novo item
     */
    private void showAddItemPopup() {
        // 1) Pega no primaryStage para posicionar o popup ao centro
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // 2) Cria o Popup
        Popup popup = new Popup();
        popup.setAutoHide(false); // Don't close automatically

        // 3) Campos do formulário
        TextField nameField = new TextField();
        nameField.setPromptText("Nome do item");
        nameField.getStyleClass().add("popup-input-field");
        nameField.setPrefHeight(40);

        ComboBox<String> tipoPratoCombo = new ComboBox<>();
        tipoPratoCombo.getItems().addAll("Entrada", "Prato Principal", "Sobremesa", "Bebida");
        tipoPratoCombo.setPromptText("Tipo de prato");
        tipoPratoCombo.getStyleClass().add("popup-combo-box");
        tipoPratoCombo.setPrefHeight(40);

        TextField precoField = new TextField();
        precoField.setPromptText("Preço (ex.: 12.99)");
        precoField.getStyleClass().add("popup-input-field");
        precoField.setPrefHeight(40);
        // valida só números e ponto
        precoField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*(\\.\\d*)?"))
                precoField.setText(o);
        });

        CheckBox compostoCheck = new CheckBox("Item composto por ingredientes");
        compostoCheck.getStyleClass().add("popup-check-box");

        // tabela de ingredientes
        TableView<IngredientRowData> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("popup-table-view");
        TableColumn<IngredientRowData, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getId()).asObject());
        idCol.setPrefWidth(40);
        TableColumn<IngredientRowData, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        nameCol.setPrefWidth(120);
        TableColumn<IngredientRowData, String> medidaCol = new TableColumn<>("Medida");
        medidaCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnitName()));
        medidaCol.setPrefWidth(80);
        TableColumn<IngredientRowData, Integer> qtyCol = new TableColumn<>("Qtd");
        qtyCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantity()).asObject());
        qtyCol.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Integer>() {
                private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
                {
                    spinner.setEditable(true);
                    spinner.valueProperty().addListener((o, ov, nv) -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            getTableRow().getItem().setQuantity(nv);
                        }
                    });
                }

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty)
                        setGraphic(null);
                    else {
                        spinner.getValueFactory().setValue(item);
                        setGraphic(spinner);
                    }
                }
            };
        });
        TableColumn<IngredientRowData, Void> actCol = new TableColumn<>("Ações");
        actCol.setCellFactory(col -> {
            return new TableCell<IngredientRowData, Void>() {

                private final Button delBtn = new Button();
                {
                    FontIcon icon = new FontIcon(MaterialDesign.MDI_DELETE);
                    icon.setIconColor(Color.RED);
                    delBtn.setGraphic(icon);
                    delBtn.getStyleClass().add("icon-button");
                    delBtn.setOnAction(e -> table.getItems().remove(getIndex()));
                }

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setGraphic(empty ? null : delBtn);
                }
            };
        });
        table.getColumns().addAll(idCol, nameCol, medidaCol, qtyCol, actCol);

        // seleção de ingredientes
        ComboBox<Ingredient> ingCombo = new ComboBox<>();
        ingCombo.getStyleClass().add("popup-combo-box");
        ingCombo.setCellFactory(lv -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient i, boolean empty) {
                super.updateItem(i, empty);
                setText((empty || i == null) ? null : i.getNome() + " (ID:" + i.getId() + ")");
            }
        });
        ingCombo.setButtonCell(ingCombo.getCellFactory().call(null));
        ingCombo.setPrefWidth(200);

        // Campo de busca para filtrar ingredientes
        TextField searchField = new TextField();
        searchField.getStyleClass().add("popup-input-field");
        searchField.setPromptText("Buscar ingrediente...");
        searchField.setPrefWidth(150);

        // Lista original de ingredientes para usar com o filtro
        List<Ingredient> originalList = new ArrayList<>();

        Button addIngBtn = new Button("Adicionar");
        addIngBtn.getStyleClass().add("popup-secondary-button");
        addIngBtn.setDisable(true);
        ingCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> addIngBtn.setDisable(n == null));
        addIngBtn.setOnAction(e -> {
            Ingredient sel = ingCombo.getValue();
            if (sel != null && table.getItems().stream().noneMatch(r -> r.getId() == sel.getId())) {
                table.getItems()
                        .add(new IngredientRowData(sel.getId(), sel.getNome(), getUnidadeName(sel.getUnidade_id()), 1));
            }
        });

        Button loadBtn = new Button("Recarregar");
        loadBtn.getStyleClass().add("popup-secondary-button");

        // Campo de busca para filtrar ingredientes
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // Se a busca estiver vazia, mostrar todos os ingredientes
                ingCombo.getItems().setAll(originalList);
            } else {
                // Filtrar ingredientes que contêm o texto da busca (ignorando
                // maiúsculas/minúsculas)
                String searchLower = newValue.toLowerCase();
                List<Ingredient> filtered = originalList.stream()
                        .filter(ing -> ing.getNome().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                ingCombo.getItems().setAll(filtered);
            }
        });

        // Carrega ingredientes automaticamente ao abrir o diálogo
        loadBtn.setDisable(true);
        loadBtn.setText("Carregando...");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET().build();
        httpClient.sendAsync(req, BodyHandlers.ofString())
                .thenAccept(resp -> {
                    List<Ingredient> list = JsonParser.parseIngredients(resp.body());
                    // Ordenar os ingredientes por nome em ordem alfabética
                    list.sort(Comparator.comparing(Ingredient::getNome));

                    Platform.runLater(() -> {
                        originalList.clear();
                        originalList.addAll(list); // Guardar lista original para filtro
                        ingCombo.getItems().setAll(list);
                        loadBtn.setText("Recarregar");
                        loadBtn.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadBtn.setText("Falha ao Carregar");
                        loadBtn.setDisable(false);
                    });
                    return null;
                });

        // Ação do botão para recarregar quando necessário
        loadBtn.setOnAction(e -> {
            loadBtn.setDisable(true);
            loadBtn.setText("Carregando...");
            HttpRequest reqReload = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                    .GET().build();
            httpClient.sendAsync(reqReload, BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        List<Ingredient> list = JsonParser.parseIngredients(resp.body());
                        // Ordenar os ingredientes por nome em ordem alfabética
                        list.sort(Comparator.comparing(Ingredient::getNome));

                        Platform.runLater(() -> {
                            originalList.clear();
                            originalList.addAll(list); // Atualizar lista original para filtro
                            ingCombo.getItems().setAll(list);
                            loadBtn.setText("Recarregar");
                            loadBtn.setDisable(false);
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            loadBtn.setText("Falha ao Carregar");
                            loadBtn.setDisable(false);
                        });
                        return null;
                    });
        });

        // 4) Botões principais
        Button submit = new Button("Adicionar");
        submit.getStyleClass().addAll("login-button", "popup-primary-button");
        Button cancel = new Button("Cancelar");
        cancel.getStyleClass().add("popup-secondary-button");
        cancel.setStyle(
                "-fx-background-color: #F5F5F5; -fx-text-fill: #666666; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand; -fx-min-width: 100;");
        submit.setDisable(true);

        // validador geral
        ChangeListener<Object> validator = (obs, o, n) -> {
            boolean nameOk = !nameField.getText().trim().isEmpty();
            boolean tipoOk = tipoPratoCombo.getValue() != null;
            boolean precoOk = !precoField.getText().trim().isEmpty();

            // If item is composite, it must have ingredients
            boolean ingredientsOk = true;
            if (compostoCheck.isSelected()) {
                ingredientsOk = !table.getItems().isEmpty();
            }

            boolean ok = nameOk && tipoOk && precoOk && ingredientsOk;

            System.out.println("DEBUG ADD - Nome: " + nameOk + " (" + nameField.getText() + "), " +
                    "Tipo: " + tipoOk + " (" + tipoPratoCombo.getValue() + "), " +
                    "Preço: " + precoOk + " (" + precoField.getText() + "), " +
                    "Composto: " + compostoCheck.isSelected() + ", " +
                    "Ingredientes: " + ingredientsOk + " (" + table.getItems().size() + "), " +
                    "Final: " + ok);

            submit.setDisable(!ok);
        };
        nameField.textProperty().addListener(validator);
        tipoPratoCombo.valueProperty().addListener(validator);
        precoField.textProperty().addListener(validator);
        compostoCheck.selectedProperty().addListener(validator);
        // Add listener to table items to validate when ingredients are added/removed
        table.getItems().addListener((javafx.collections.ListChangeListener<IngredientRowData>) change -> {
            validator.changed(null, null, null);
        });

        // Trigger initial validation
        Platform.runLater(() -> validator.changed(null, null, null));

        // 5) Layout moderno com dimensões apropriadas
        VBox popupContent = new VBox(25);
        popupContent.setPadding(new Insets(30));
        popupContent.getStyleClass().add("popup-container");
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);" +
                        "-fx-border-radius: 12;" +
                        "-fx-min-width: 800;" +
                        "-fx-min-height: 750;");

        // Header with title and close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        Label titleLabel = new Label("Adicionar Item");
        titleLabel.getStyleClass().add("popup-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FB8C00;");

        // Spacer to push close button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button (X)
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("popup-close-button");
        closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;");

        closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                "-fx-background-color: #f0f0f0; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;"));

        closeButton.setOnMouseExited(e -> closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;"));

        // Close button action with validation
        closeButton.setOnAction(e -> {
            boolean hasData = !nameField.getText().trim().isEmpty() ||
                    tipoPratoCombo.getValue() != null ||
                    !precoField.getText().trim().isEmpty() ||
                    compostoCheck.isSelected() ||
                    !table.getItems().isEmpty();

            if (hasData) {
                PopUp.showConfirmationPopup(
                        Alert.AlertType.WARNING,
                        "Confirmar Saída",
                        "Dados não salvos serão perdidos",
                        "Tem a certeza que deseja fechar sem guardar as alterações?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        headerBox.getChildren().addAll(titleLabel, spacer, closeButton);

        Label headerLabel = new Label("Preencha os detalhes do novo item");
        headerLabel.getStyleClass().add("popup-subtitle");
        headerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // Form grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(15));

        // Labels estilizadas
        Label nomeLabel = new Label("Nome:");
        nomeLabel.getStyleClass().add("popup-label");
        nomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        Label tipoLabel = new Label("Tipo de Prato:");
        tipoLabel.getStyleClass().add("popup-label");
        tipoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        Label precoLabel = new Label("Preço:");
        precoLabel.getStyleClass().add("popup-label");
        precoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        grid.add(nomeLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(tipoLabel, 0, 1);
        grid.add(tipoPratoCombo, 1, 1);
        grid.add(precoLabel, 0, 2);
        grid.add(precoField, 1, 2);
        grid.add(compostoCheck, 0, 3, 2, 1);

        // Seção de ingredientes
        TitledPane ingredientsPane = new TitledPane();
        ingredientsPane.setText("Ingredientes do Item");
        ingredientsPane.setExpanded(false);
        ingredientsPane.setPrefHeight(350);

        VBox ingredientsContainer = new VBox(10);
        ingredientsContainer.setPadding(new Insets(10));

        // Layout para pesquisa e seleção de ingredientes
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 5, 0));

        Label searchLabel = new Label("Buscar:");
        searchLabel.getStyleClass().add("popup-label");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");
        searchBox.getChildren().addAll(searchLabel, searchField);

        HBox ingLoadBox = new HBox(10, loadBtn, ingCombo, addIngBtn);
        ingLoadBox.setAlignment(Pos.CENTER_LEFT);

        VBox ingSelectionBox = new VBox(10, searchBox, ingLoadBox);
        ingSelectionBox.setPadding(new Insets(5, 0, 10, 0));

        ingredientsContainer.getChildren().addAll(ingSelectionBox, table);
        ingredientsPane.setContent(ingredientsContainer);

        grid.add(ingredientsPane, 0, 4, 2, 1);
        GridPane.setVgrow(ingredientsPane, Priority.ALWAYS);
        GridPane.setHgrow(ingredientsPane, Priority.ALWAYS);

        // Buttons section
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getStyleClass().add("popup-button-section");
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.setStyle("-fx-border-color: #E8E8E8 transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        buttonBox.getChildren().addAll(cancel, submit);

        // Add all components to popup content
        popupContent.getChildren().addAll(headerBox, headerLabel, grid, buttonBox);
        popup.getContent().add(popupContent);

        // 6) Mostra o popup centrado
        popup.show(primary, centerX - 400, centerY - 350);

        // Focus on name field when opened
        Platform.runLater(() -> nameField.requestFocus());

        // 7) Ações
        cancel.setOnAction(e -> {
            boolean hasData = !nameField.getText().trim().isEmpty() ||
                    tipoPratoCombo.getValue() != null ||
                    !precoField.getText().trim().isEmpty() ||
                    compostoCheck.isSelected() ||
                    !table.getItems().isEmpty();

            if (hasData) {
                PopUp.showConfirmationPopup(
                        Alert.AlertType.WARNING,
                        "Confirmar Saída",
                        "Dados não salvos serão perdidos",
                        "Tem a certeza que deseja cancelar? Todos os dados inseridos serão perdidos.",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });
        submit.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int tipoId = tipoPratoCombo.getSelectionModel().getSelectedIndex() + 1;
            double preco = Double.parseDouble(precoField.getText());
            boolean composto = compostoCheck.isSelected();
            List<Item.ItemIngrediente> ingList = new ArrayList<>();
            for (IngredientRowData row : table.getItems()) {
                Item.ItemIngrediente ii = new Item.ItemIngrediente();
                ii.setIngredienteId(row.getId());
                ii.setQuantidade(row.getQuantity());
                ingList.add(ii);
            }
            createItem(nome, tipoId, preco, composto, ingList);
            popup.hide();
        });
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
            List<Item.ItemIngrediente> ingredientes) {
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
            // Adicionar ingredientes (sempre incluir o campo, mesmo que vazio)
            jsonBuilder.append("\"ingredientes\":[");

            // Se for composto e tiver ingredientes, adicionar cada um
            if (ingredientes != null && !ingredientes.isEmpty()) {
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
            }
            jsonBuilder.append("]");
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
                                PopUp.showItemCreateSuccess();

                                // Recarregar a lista de itens
                                show();
                            } else {
                                // Erro
                                PopUp.showItemCreateError(resp.statusCode());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();

                        Platform.runLater(() -> {
                            PopUp.showItemCreateError(ex.getMessage());
                        });

                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                PopUp.showItemCreateError(e.getMessage());
            });
        }
    }

    /**
     * Classe auxiliar para representar uma linha na tabela de ingredientes
     */
    private static class IngredientRowData {
        private final int id;
        private final String name;
        private final String unitName;
        private int quantity;

        public IngredientRowData(int id, String name, String unitName, int quantity) {
            this.id = id;
            this.name = name;
            this.unitName = unitName;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUnitName() {
            return unitName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * Exibe o diálogo para editar um item existente
     * 
     * @param item Item a ser editado
     */
    private void showEditItemPopup(Item item) {
        // 1) Pega no primaryStage para posicionar o popup ao centro
        Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // 2) Cria o Popup
        Popup popup = new Popup();
        popup.setAutoHide(false); // Don't close automatically

        // 3) Main container with modern styling
        VBox popupContent = new VBox(25);
        popupContent.setPadding(new Insets(30));
        popupContent.getStyleClass().add("popup-container");
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);" +
                        "-fx-border-radius: 12;" +
                        "-fx-min-width: 750;" +
                        "-fx-min-height: 750;");

        // Header with title and close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));

        Label titleLabel = new Label("Editar Item");
        titleLabel.getStyleClass().add("popup-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FB8C00;");

        // Spacer to push close button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button (X)
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("popup-close-button");
        closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;");

        closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                "-fx-background-color: #f0f0f0; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;"));

        closeButton.setOnMouseExited(e -> closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 15; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30;"));

        // Store original values for comparison
        final String originalName = item.getNome();
        final int originalTipoPratoId = item.getTipoPratoId();
        final double originalPreco = item.getPreco();
        final boolean originalComposto = item.isEComposto();
        final List<Item.ItemIngrediente> originalIngredients = item.getIngredientes() != null
                ? new ArrayList<>(item.getIngredientes())
                : new ArrayList<>();

        headerBox.getChildren().addAll(titleLabel, spacer, closeButton);

        Label headerLabel = new Label("Atualize as informações do item");
        headerLabel.getStyleClass().add("popup-subtitle");
        headerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // 4) Campos do formulário pré-preenchidos
        TextField nameField = new TextField(item.getNome());
        nameField.setPromptText("Nome do item");
        nameField.getStyleClass().add("popup-input-field");
        nameField.setPrefHeight(40);

        ComboBox<String> tipoPratoCombo = new ComboBox<>();
        tipoPratoCombo.getItems().addAll("Entrada", "Prato Principal", "Sobremesa", "Bebida");
        tipoPratoCombo.getSelectionModel().select(item.getTipoPratoId() - 1);
        tipoPratoCombo.getStyleClass().add("popup-combo-box");
        tipoPratoCombo.setPrefHeight(40);

        TextField precoField = new TextField(String.format("%.2f", item.getPreco()));
        precoField.setPromptText("Preço (ex.: 12.99)");
        precoField.getStyleClass().add("popup-input-field");
        precoField.setPrefHeight(40);
        precoField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*(\\.\\d*)?")) {
                precoField.setText(o);
            }
        });

        CheckBox compostoCheck = new CheckBox("Item composto por ingredientes");
        compostoCheck.getStyleClass().add("popup-check-box");
        System.out.println("DEBUG: Item " + item.getNome() + " - isEComposto: " + item.isEComposto());
        compostoCheck.setSelected(item.isEComposto());

        // Form grid with modern styling
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(15));

        // Labels estilizadas
        Label nomeLabel = new Label("Nome:");
        nomeLabel.getStyleClass().add("popup-label");
        nomeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        Label tipoLabel = new Label("Tipo de Prato:");
        tipoLabel.getStyleClass().add("popup-label");
        tipoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        Label precoLabel = new Label("Preço:");
        precoLabel.getStyleClass().add("popup-label");
        precoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");

        grid.add(nomeLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(tipoLabel, 0, 1);
        grid.add(tipoPratoCombo, 1, 1);
        grid.add(precoLabel, 0, 2);
        grid.add(precoField, 1, 2);
        grid.add(compostoCheck, 0, 3, 2, 1);

        // 5) Tabela de ingredientes
        TableView<IngredientRowData> table = new TableView<>();
        table.setPrefHeight(200);
        table.getStyleClass().add("popup-table-view");
        TableColumn<IngredientRowData, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getId()).asObject());
        idCol.setPrefWidth(40);
        TableColumn<IngredientRowData, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));
        nameCol.setPrefWidth(120);
        TableColumn<IngredientRowData, String> medidaCol = new TableColumn<>("Medida");
        medidaCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUnitName()));
        medidaCol.setPrefWidth(80);
        TableColumn<IngredientRowData, Integer> qtyCol = new TableColumn<>("Qtd");
        qtyCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getQuantity()).asObject());
        qtyCol.setPrefWidth(80);
        qtyCol.setCellFactory(col -> new TableCell<IngredientRowData, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);
            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((o, ov, nv) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setQuantity(nv);
                    }
                });
            }

            @Override
            protected void updateItem(Integer itemValue, boolean empty) {
                super.updateItem(itemValue, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(itemValue);
                    setGraphic(spinner);
                }
            }
        });
        TableColumn<IngredientRowData, Void> actCol = new TableColumn<>("Ações");
        actCol.setCellFactory(col -> new TableCell<IngredientRowData, Void>() {
            private final Button delBtn = new Button();
            {
                FontIcon icon = new FontIcon(MaterialDesign.MDI_DELETE);
                icon.setIconColor(Color.RED);
                delBtn.setGraphic(icon);
                delBtn.getStyleClass().add("icon-button");
                delBtn.setOnAction(e -> table.getItems().remove(getIndex()));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : delBtn);
            }
        });
        table.getColumns().addAll(idCol, nameCol, medidaCol, qtyCol, actCol);

        // 6) ComboBox para adicionar ingredientes
        ComboBox<Ingredient> ingCombo = new ComboBox<>();
        ingCombo.getStyleClass().add("popup-combo-box");
        ingCombo.setCellFactory(lv -> new ListCell<Ingredient>() {
            @Override
            protected void updateItem(Ingredient i, boolean empty) {
                super.updateItem(i, empty);
                setText(empty || i == null ? null : i.getNome() + " (ID:" + i.getId() + ")");
            }
        });
        ingCombo.setButtonCell(ingCombo.getCellFactory().call(null));
        ingCombo.setPrefWidth(200);

        // Campo de busca para filtrar ingredientes
        TextField searchField = new TextField();
        searchField.getStyleClass().add("popup-input-field");
        searchField.setPromptText("Buscar ingrediente...");
        searchField.setPrefWidth(150);

        // Lista original de ingredientes para usar com o filtro
        List<Ingredient> originalList = new ArrayList<>();

        Button addIngBtn = new Button("Adicionar");
        addIngBtn.getStyleClass().add("popup-secondary-button");
        addIngBtn.setDisable(true);
        ingCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> addIngBtn.setDisable(n == null));
        addIngBtn.setOnAction(e -> {
            Ingredient sel = ingCombo.getValue();
            if (sel != null && table.getItems().stream().noneMatch(r -> r.getId() == sel.getId())) {
                table.getItems()
                        .add(new IngredientRowData(sel.getId(), sel.getNome(), getUnidadeName(sel.getUnidade_id()), 1));
            }
        });

        Button loadBtn = new Button("Recarregar");
        loadBtn.getStyleClass().add("popup-secondary-button");

        // Campo de busca para filtrar ingredientes
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                // Se a busca estiver vazia, mostrar todos os ingredientes
                ingCombo.getItems().setAll(originalList);
            } else {
                // Filtrar ingredientes que contêm o texto da busca (ignorando
                // maiúsculas/minúsculas)
                String searchLower = newValue.toLowerCase();
                List<Ingredient> filtered = originalList.stream()
                        .filter(ing -> ing.getNome().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
                ingCombo.getItems().setAll(filtered);
            }
        });

        // Carrega ingredientes automaticamente ao abrir o diálogo
        loadBtn.setDisable(true);
        loadBtn.setText("Carregando...");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                .GET().build();
        httpClient.sendAsync(req, BodyHandlers.ofString())
                .thenAccept(resp -> {
                    List<Ingredient> list = JsonParser.parseIngredients(resp.body());
                    // Ordenar os ingredientes por nome em ordem alfabética
                    list.sort(Comparator.comparing(Ingredient::getNome));

                    Platform.runLater(() -> {
                        originalList.clear();
                        originalList.addAll(list); // Guardar lista original para filtro
                        ingCombo.getItems().setAll(list);

                        // pré-carrega os existentes do item
                        if (item.getIngredientes() != null) {
                            table.getItems().clear();
                            for (Item.ItemIngrediente ii : item.getIngredientes()) {
                                Ingredient ing = list.stream()
                                        .filter(x -> x.getId() == ii.getIngredienteId())
                                        .findFirst()
                                        .orElse(null);

                                String nome = ing != null ? ing.getNome() : "ID:" + ii.getIngredienteId();
                                String unitName = ing != null ? getUnidadeName(ing.getUnidade_id()) : "Desconhecido";

                                table.getItems().add(
                                        new IngredientRowData(ii.getIngredienteId(), nome, unitName,
                                                ii.getQuantidade()));
                            }
                        }
                        loadBtn.setText("Recarregar");
                        loadBtn.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadBtn.setText("Falha ao Carregar");
                        loadBtn.setDisable(false);
                    });
                    return null;
                });

        // Ação do botão para recarregar quando necessário
        loadBtn.setOnAction(e -> {
            loadBtn.setDisable(true);
            loadBtn.setText("Carregando...");
            HttpRequest reqReload = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/getAll")))
                    .GET().build();
            httpClient.sendAsync(reqReload, BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        List<Ingredient> list = JsonParser.parseIngredients(resp.body());
                        // Ordenar os ingredientes por nome em ordem alfabética
                        list.sort(Comparator.comparing(Ingredient::getNome));

                        Platform.runLater(() -> {
                            originalList.clear();
                            originalList.addAll(list); // Atualizar lista original para filtro
                            ingCombo.getItems().setAll(list);

                            // pré-carrega os existentes do item
                            if (item.getIngredientes() != null) {
                                table.getItems().clear();
                                for (Item.ItemIngrediente ii : item.getIngredientes()) {
                                    Ingredient ing = list.stream()
                                            .filter(x -> x.getId() == ii.getIngredienteId())
                                            .findFirst()
                                            .orElse(null);

                                    String nome = ing != null ? ing.getNome() : "ID:" + ii.getIngredienteId();
                                    String unitName = ing != null ? getUnidadeName(ing.getUnidade_id())
                                            : "Desconhecido";

                                    table.getItems().add(
                                            new IngredientRowData(ii.getIngredienteId(), nome, unitName,
                                                    ii.getQuantidade()));
                                }
                            }
                            loadBtn.setText("Recarregar");
                            loadBtn.setDisable(false);
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            loadBtn.setText("Falha ao Carregar");
                            loadBtn.setDisable(false);
                        });
                        return null;
                    });
        });

        // 7) Botões Guardar e Cancelar
        Button saveBtn = new Button("Guardar Alterações");
        saveBtn.getStyleClass().add("popup-primary-button");
        Button cancelBtn = new Button("Cancelar");
        cancelBtn.getStyleClass().add("popup-secondary-button");
        saveBtn.setDisable(true);

        // 8) Validação geral
        ChangeListener<Object> validator = (obs, o, n) -> {
            boolean nameOk = !nameField.getText().trim().isEmpty();
            boolean tipoOk = tipoPratoCombo.getValue() != null;
            boolean precoOk = !precoField.getText().trim().isEmpty();

            // If item is composite, it must have ingredients
            boolean ingredientsOk = true;
            if (compostoCheck.isSelected()) {
                ingredientsOk = !table.getItems().isEmpty();
            }

            boolean ok = nameOk && tipoOk && precoOk && ingredientsOk;

            System.out.println("DEBUG EDIT - Nome: " + nameOk + " (" + nameField.getText() + "), " +
                    "Tipo: " + tipoOk + " (" + tipoPratoCombo.getValue() + "), " +
                    "Preço: " + precoOk + " (" + precoField.getText() + "), " +
                    "Composto: " + compostoCheck.isSelected() + ", " +
                    "Ingredientes: " + ingredientsOk + " (" + table.getItems().size() + "), " +
                    "Final: " + ok);

            saveBtn.setDisable(!ok);
        };
        nameField.textProperty().addListener(validator);
        tipoPratoCombo.valueProperty().addListener(validator);
        precoField.textProperty().addListener(validator);
        compostoCheck.selectedProperty().addListener(validator);
        // Add listener to table items to validate when ingredients are added/removed
        table.getItems().addListener((javafx.collections.ListChangeListener<IngredientRowData>) change -> {
            validator.changed(null, null, null);
        });

        // Chama o validador uma vez para avaliar o estado inicial
        validator.changed(null, null, null);

        // Seção de ingredientes
        TitledPane ingredientsPane = new TitledPane();
        ingredientsPane.setText("Ingredientes do Item");
        ingredientsPane.setExpanded(false);
        ingredientsPane.setPrefHeight(350);

        VBox ingredientsContainer = new VBox(10);
        ingredientsContainer.setPadding(new Insets(10));

        // Layout para pesquisa e seleção de ingredientes
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 5, 0));

        Label searchLabel = new Label("Buscar:");
        searchLabel.getStyleClass().add("popup-label");
        searchLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #444444;");
        searchBox.getChildren().addAll(searchLabel, searchField);

        HBox ingLoadBox = new HBox(10, loadBtn, ingCombo, addIngBtn);
        ingLoadBox.setAlignment(Pos.CENTER_LEFT);

        VBox ingSelectionBox = new VBox(10, searchBox, ingLoadBox);
        ingSelectionBox.setPadding(new Insets(5, 0, 10, 0));

        ingredientsContainer.getChildren().addAll(ingSelectionBox, table);
        ingredientsPane.setContent(ingredientsContainer);

        grid.add(ingredientsPane, 0, 4, 2, 1);
        GridPane.setVgrow(ingredientsPane, Priority.ALWAYS);
        GridPane.setHgrow(ingredientsPane, Priority.ALWAYS);

        // Buttons section
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getStyleClass().add("popup-button-section");
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.setStyle("-fx-border-color: #E8E8E8 transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        // Add all components to popup content
        popupContent.getChildren().addAll(headerBox, headerLabel, grid, buttonBox);
        popup.getContent().add(popupContent);

        // Close button action with validation (now that all components are available)
        closeButton.setOnAction(e -> {
            boolean hasChanges = !nameField.getText().trim().equals(originalName) ||
                    tipoPratoCombo.getSelectionModel().getSelectedIndex() + 1 != originalTipoPratoId ||
                    !precoField.getText().trim().equals(String.format("%.2f", originalPreco)) ||
                    compostoCheck.isSelected() != originalComposto ||
                    hasIngredientChanges(table.getItems(), originalIngredients);

            if (hasChanges) {
                PopUp.showConfirmationPopup(
                        Alert.AlertType.WARNING,
                        "Confirmar Saída",
                        "Alterações não guardadas serão perdidas",
                        "Tem a certeza que deseja fechar sem guardar as alterações?",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });

        // 9) Ações finais
        cancelBtn.setOnAction(e -> {
            boolean hasChanges = !nameField.getText().trim().equals(originalName) ||
                    tipoPratoCombo.getSelectionModel().getSelectedIndex() + 1 != originalTipoPratoId ||
                    !precoField.getText().trim().equals(String.format("%.2f", originalPreco)) ||
                    compostoCheck.isSelected() != originalComposto ||
                    hasIngredientChanges(table.getItems(), originalIngredients);

            if (hasChanges) {
                PopUp.showConfirmationPopup(
                        Alert.AlertType.WARNING,
                        "Confirmar Cancelamento",
                        "Alterações não guardadas serão perdidas",
                        "Tem a certeza que deseja cancelar? Todas as alterações serão perdidas.",
                        () -> popup.hide());
            } else {
                popup.hide();
            }
        });
        saveBtn.setOnAction(e -> {
            String nome = nameField.getText().trim();
            int tipoId = tipoPratoCombo.getSelectionModel().getSelectedIndex() + 1;
            double preco = Double.parseDouble(precoField.getText());
            boolean composto = compostoCheck.isSelected();
            List<Item.ItemIngrediente> ingList = new ArrayList<>();
            for (IngredientRowData row : table.getItems()) {
                Item.ItemIngrediente ii = new Item.ItemIngrediente();
                ii.setIngredienteId(row.getId());
                ii.setQuantidade(row.getQuantity());
                ingList.add(ii);
            }
            updateItem(item.getId(), nome, tipoId, preco, composto, ingList);
            popup.hide();
        });

        // 10) Mostra o popup centrado
        popup.show(primary, centerX - 375, centerY - 375);

        // Focus on name field when opened
        Platform.runLater(() -> nameField.requestFocus());
    }

    /**
     * Envia uma requisição para a API para atualizar um item existente
     *
     * @param id           ID do item a ser atualizado
     * @param nome         Nome do item
     * @param tipoPratoId  ID do tipo de prato
     * @param preco        Preço do item
     * @param composto     Indica se o item é composto de ingredientes
     * @param ingredientes Lista de ingredientes do item (apenas para itens
     *                     compostos)
     */
    private void updateItem(int id, String nome, int tipoPratoId, double preco, boolean composto,
            List<Item.ItemIngrediente> ingredientes) {
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
            // Adicionar ingredientes (sempre incluir o campo, mesmo que vazio)
            jsonBuilder.append("\"ingredientes\":[");

            // Se for composto e tiver ingredientes, adicionar cada um
            if (ingredientes != null && !ingredientes.isEmpty()) {
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
            }

            jsonBuilder.append("]");
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
                                PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Item Atualizado",
                                        "O item foi atualizado com sucesso!");

                                // Recarregar a lista de itens
                                show();
                            } else {
                                // Erro
                                PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar item",
                                        "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();

                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar item",
                                    "Ocorreu um erro ao tentar enviar os dados: " + ex.getMessage());
                        });

                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao atualizar item",
                        "Ocorreu um erro ao processar os dados: " + e.getMessage());
            });
        }
    }

    /**
     * Exibe diálogo de confirmação para excluir um item
     * 
     * @param item Item a ser excluído
     */
    private void confirmDeleteItem(Item item) {
        PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION, "Confirmar Exclusão", "Excluir Item",
                "Tem certeza que deseja excluir o item \"" + item.getNome() + "\"?",
                () -> deleteItem(item.getId()));
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
                            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Item Excluído",
                                    "O item foi excluído com sucesso!");

                            // Recarregar a lista de itens
                            show();
                        } else {
                            // Erro
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir item",
                                    "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();

                    Platform.runLater(() -> {
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir item",
                                "Ocorreu um erro ao tentar enviar a solicitação: " + ex.getMessage());
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
            PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Informação", "Item Simples",
                    "Este item não é composto, portanto não possui ingredientes associados.");
            return;
        }

        if (item.getIngredientes() == null || item.getIngredientes().isEmpty()) {
            // Show dialog with raw JSON for debugging purposes
            TextArea textArea = new TextArea();
            textArea.setText("Este item deveria ter ingredientes, mas nenhum foi encontrado.\n\n" +
                    "JSON Original: " + item.getIngredientes() + "\n\n" +
                    "Isso pode indicar um problema no formato do JSON recebido da API.");
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(200);

            showCustomContentPopup(Alert.AlertType.WARNING, "Problema de Formato",
                    "Problemas ao Processar Ingredientes", textArea);
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
                            TextArea textArea = new TextArea(info.toString());
                            textArea.setEditable(false);
                            textArea.setWrapText(true);
                            textArea.setPrefWidth(400);
                            textArea.setPrefHeight(300);

                            parentDialog.setHeaderText("Ingredientes de " + item.getNome());
                            showCustomContentPopup(Alert.AlertType.INFORMATION, "Ingredientes Completos",
                                    "Ingredientes de " + item.getNome(), textArea);
                        });
                    } else {
                        Platform.runLater(() -> {
                            parentDialog.setHeaderText("Ingredientes de " + item.getNome());
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingredientes",
                                    "Status code: " + resp.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        parentDialog.setHeaderText("Ingredientes de " + item.getNome());
                        PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingredientes",
                                "Erro: " + ex.getMessage());
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
        // Show loading popup
        Stage primaryStage = StageManager.getPrimaryStage();
        double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;

        Popup loadingPopup = new Popup();
        VBox loadingContent = new VBox(10);
        loadingContent.setPadding(new Insets(20));
        loadingContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        loadingContent.setAlignment(Pos.CENTER);

        ProgressIndicator progress = new ProgressIndicator();
        Label loadingLabel = new Label("Carregando detalhes do ingrediente...");
        loadingContent.getChildren().addAll(progress, loadingLabel);
        loadingPopup.getContent().add(loadingContent);

        // Show loading popup
        Platform.runLater(() -> {
            loadingPopup.show(primaryStage, centerX - 100, centerY - 50);

            // Fazer requisição à API para obter o ingrediente
            HttpRequest getIngredientReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/ingredientes/get?id=" + ingredientId)))
                    .GET()
                    .build();

            httpClient.sendAsync(getIngredientReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        loadingPopup.hide();

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
                                    PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro",
                                            "Erro ao processar dados do ingrediente",
                                            "Ocorreu um erro ao processar os dados. " + e.getMessage());
                                });
                            }
                        } else {
                            Platform.runLater(() -> {
                                PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingrediente",
                                        "Status code: " + resp.statusCode());
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        loadingPopup.hide();
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao carregar ingrediente",
                                    "Erro: " + ex.getMessage());
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

        showCustomContentPopup(Alert.AlertType.INFORMATION, "Detalhes do Ingrediente",
                ingredient.getNome(), grid);
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

    /**
     * Creates and shows a popup dialog with custom content
     * 
     * @param type          The type of alert (affects title color)
     * @param title         The title of the dialog
     * @param header        The header text of the dialog
     * @param customContent The custom content node to display
     */
    private void showCustomContentPopup(Alert.AlertType type, String title, String header, Node customContent) {
        Stage primaryStage = StageManager.getPrimaryStage();
        double centerX = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerY = primaryStage.getY() + primaryStage.getHeight() / 2;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        // Create the popup content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        popupContent.setMinWidth(400);
        popupContent.setMaxWidth(600);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Set title color based on type
        Color titleColor = Color.BLACK;
        if (type == Alert.AlertType.ERROR) {
            titleColor = Color.RED;
        } else if (type == Alert.AlertType.WARNING) {
            titleColor = Color.ORANGE;
        } else if (type == Alert.AlertType.INFORMATION) {
            titleColor = Color.BLUE;
        }
        titleLabel.setTextFill(titleColor);

        // Header
        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        headerLabel.setWrapText(true);

        // OK Button
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("login-button");
        okButton.setOnAction(e -> popup.hide());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(okButton);

        popupContent.getChildren().addAll(titleLabel, headerLabel, customContent, buttonBox);
        popup.getContent().add(popupContent);

        // Show popup centered
        popup.show(primaryStage, centerX - 200, centerY - 150);
    }

    /**
     * Helper method to check if there are changes in the ingredients list
     * 
     * @param currentIngredients  Current ingredients in the table
     * @param originalIngredients Original ingredients from the item
     * @return true if there are changes, false otherwise
     */
    private boolean hasIngredientChanges(ObservableList<IngredientRowData> currentIngredients,
            List<Item.ItemIngrediente> originalIngredients) {
        // Check if sizes are different
        if (currentIngredients.size() != originalIngredients.size()) {
            return true;
        }

        // Convert current ingredients to a map for easier comparison
        Map<Integer, Integer> currentMap = new HashMap<>();
        for (IngredientRowData current : currentIngredients) {
            currentMap.put(current.getId(), current.getQuantity());
        }

        // Check each original ingredient
        for (Item.ItemIngrediente original : originalIngredients) {
            Integer currentQuantity = currentMap.get(original.getIngredienteId());
            if (currentQuantity == null || !currentQuantity.equals(original.getQuantidade())) {
                return true;
            }
        }

        return false;
    }
}
