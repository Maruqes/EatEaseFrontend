package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.Menu;
import com.EatEaseFrontend.StageManager;
import com.EatEaseFrontend.TipoMenu;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * View para gerenciar e exibir menus
 */
public class MenuView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

    public MenuView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
    }

    /**
     * Carrega e exibe a lista de menus (SÍNCRONO)
     */
    public void show() {
        System.out.println("[MENU] Iniciando carregamento SÍNCRONO de menus...");

        contentArea.getChildren().clear();
        ProgressIndicator progress = new ProgressIndicator();
        Text loading = new Text("Carregando menus...");
        loading.getStyleClass().add("welcome-text");
        VBox loader = new VBox(20, progress, loading);
        loader.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loader);

        HttpRequest reqMenus = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/getAll")))
                .GET()
                .build();
        HttpRequest reqTipos = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/tipomenu/getAll")))
                .GET()
                .build();

        try {
            // Requisições SÍNCRONAS - aguarda resposta antes de continuar
            System.out.println("[MENU] Fazendo requisição síncrona para carregar menus...");
            HttpResponse<String> respMenus = httpClient.send(reqMenus, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta de menus recebida - Status: " + respMenus.statusCode());
            if (respMenus.statusCode() == 200) {
                List<Menu> menus = JsonParser.parseMenus(respMenus.body());
                System.out.println("[MENU] Menus parseados com sucesso. Total: " + menus.size());

                System.out.println("[MENU] Fazendo requisição síncrona para carregar tipos de menu...");
                HttpResponse<String> respTipos = httpClient.send(reqTipos, HttpResponse.BodyHandlers.ofString());

                System.out.println("[MENU] Resposta de tipos recebida - Status: " + respTipos.statusCode());
                if (respTipos.statusCode() == 200) {
                    List<TipoMenu> tipos = JsonParser.parseTipoMenus(respTipos.body());
                    System.out.println("[MENU] Tipos de menu parseados com sucesso. Total: " + tipos.size());

                    Platform.runLater(() -> {
                        System.out.println("[MENU] Atualizando UI com dados carregados sincronamente");
                        displayMenusAsCards(menus, tipos);
                    });
                } else {
                    System.err.println("[MENU] Erro HTTP ao carregar tipos de menu. Status: " + respTipos.statusCode());
                    showError("Falha ao carregar tipos de menu", respTipos.statusCode());
                }
            } else {
                System.err.println("[MENU] Erro HTTP ao carregar menus. Status: " + respMenus.statusCode());
                showError("Falha ao carregar menus", respMenus.statusCode());
            }
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA no show(): " + ex.getMessage());
            ex.printStackTrace();
            showError("Falha ao carregar menus", ex.getMessage());
        }
    }

    private void showError(String header, int status) {
        Platform.runLater(() -> {
            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", header, "Status code: " + status);
        });
    }

    private void showError(String header, String message) {
        Platform.runLater(() -> {
            PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", header, "Erro: " + message);
        });
    }

    /**
     * Exibe os menus como cards em um FlowPane
     */
    private void displayMenusAsCards(List<Menu> menus, List<TipoMenu> tipos) {
        contentArea.getChildren().clear();

        ScrollPane pane = new ScrollPane();
        pane.setFitToWidth(true);
        pane.setPadding(new Insets(20));

        FlowPane cards = new FlowPane();
        cards.setHgap(20);
        cards.setVgap(20);
        cards.setPadding(new Insets(20));

        VBox box = new VBox(20);
        HBox headerBox = new HBox();
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        // Título da seção
        Label title = new Label("Menus");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerBox.getChildren().add(title);

        // Adicionar spacer para empurrar o botão para a direita
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Criar botão de adicionar
        Button addButton = new Button("Adicionar");
        addButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLUS));
        addButton.getStyleClass().add("login-button");
        addButton.setOnAction(e -> showAddMenuDialog(tipos));

        headerBox.getChildren().addAll(spacer, addButton);
        box.getChildren().add(headerBox);

        if (menus.isEmpty()) {
            Label none = new Label("Nenhum menu encontrado");
            none.setFont(Font.font("System", FontWeight.NORMAL, 18));
            box.getChildren().add(none);
        } else {
            Map<Integer, String> mapa = tipos.stream()
                    .collect(Collectors.toMap(TipoMenu::getId, TipoMenu::getNome));
            for (Menu m : menus) {
                VBox card = new VBox(10);
                card.getStyleClass().add("dashboard-card");
                card.setPrefWidth(300);
                card.setPrefHeight(180);
                card.setPadding(new Insets(15));

                // Nome
                Label nm = new Label(m.getNome());
                nm.setFont(Font.font("System", FontWeight.BOLD, 18));
                nm.getStyleClass().add("card-title");

                // Descrição
                Label desc = new Label("Descrição: " + m.getDescricao());
                desc.setWrapText(true); // permite quebrar linha automaticamente
                desc.setMaxWidth(270); // largura máxima dentro do card (ajusta se precisares)
                desc.setFont(Font.font("System", 14)); // tamanho de letra maior se quiseres mais destaque

                // Tipo
                Label tp = new Label("Tipo: " + mapa.getOrDefault(m.getTipoMenuId(), ""));

                // Botões de ação
                HBox buttonsBox = new HBox(10);
                buttonsBox.setAlignment(Pos.CENTER_RIGHT);
                buttonsBox.setPadding(new Insets(10, 0, 0, 0));

                // Botão Editar
                Button editButton = new Button("");
                editButton.setTooltip(new Tooltip("Editar"));
                FontIcon editIcon = new FontIcon(MaterialDesign.MDI_PENCIL);
                editIcon.setIconColor(Color.BLUE);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("icon-button");
                editButton.setOnAction(e -> {
                    showEditMenuDialog(m, tipos);
                });

                // Botão Excluir
                Button deleteButton = new Button("");
                deleteButton.setTooltip(new Tooltip("Excluir"));
                FontIcon deleteIcon = new FontIcon(MaterialDesign.MDI_DELETE);
                deleteIcon.setIconColor(Color.RED);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("icon-button");
                deleteButton.setOnAction(e -> {
                    PopUp.showConfirmationPopup(Alert.AlertType.CONFIRMATION,
                            "Confirmação de Exclusão",
                            "Tem certeza que deseja excluir este menu?",
                            "Esta ação não pode ser desfeita.", () -> {
                                deleteMenu(m.getId());
                            });
                });

                buttonsBox.getChildren().addAll(editButton, deleteButton);

                // Adicionar ao card
                card.getChildren().addAll(nm, desc, tp, buttonsBox);
                cards.getChildren().add(card);
            }
            box.getChildren().add(cards);
        }

        pane.setContent(box);
        contentArea.getChildren().add(pane);
    }

    /**
     * Exibe o diálogo para adicionar um novo menu
     * 
     * @param tiposMenu Lista de tipos de menu disponíveis
     */
    private void showAddMenuDialog(List<TipoMenu> tiposMenu) {
        // Get primary stage for positioning
        javafx.stage.Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // Create popup
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(false); // Don't close automatically

        // Create content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        popupContent.setPrefWidth(700);
        popupContent.setPrefHeight(700);

        // Title
        Label titleLabel = new Label("Adicionar Menu");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Header
        Label headerLabel = new Label("Preencha os detalhes do novo menu");
        headerLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Campo para nome do menu
        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome do menu");

        // Campo para descrição do menu
        TextArea descricaoField = new TextArea();
        descricaoField.setPromptText("Descrição do menu");
        descricaoField.setPrefRowCount(3);
        descricaoField.setWrapText(true);

        // Dropdown para tipo de menu
        ComboBox<TipoMenu> tipoMenuComboBox = new ComboBox<>();
        tipoMenuComboBox.getItems().addAll(tiposMenu);
        tipoMenuComboBox.setCellFactory(lv -> new ListCell<TipoMenu>() {
            @Override
            protected void updateItem(TipoMenu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNome());
            }
        });
        tipoMenuComboBox.setButtonCell(new ListCell<TipoMenu>() {
            @Override
            protected void updateItem(TipoMenu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o tipo de menu" : item.getNome());
            }
        });

        // Área de seleção de itens
        TitledPane itemsPane = new TitledPane();
        itemsPane.setText("Selecione os Itens para o Menu");
        itemsPane.setExpanded(false);
        itemsPane.setPrefHeight(400); // Make the pane larger

        VBox itemsContainer = new VBox(10);
        itemsContainer.setPadding(new Insets(10));

        Label loadingItemsLabel = new Label("Carregando itens disponíveis...");
        ProgressIndicator loadingProgress = new ProgressIndicator();
        loadingProgress.setPrefSize(20, 20);

        HBox loadingBox = new HBox(10, loadingProgress, loadingItemsLabel);
        loadingBox.setAlignment(Pos.CENTER_LEFT);

        itemsContainer.getChildren().add(loadingBox);
        itemsPane.setContent(itemsContainer);

        // Lista observável de IDs dos itens selecionados
        ObservableList<Integer> selectedItemsIds = FXCollections.observableArrayList();

        // Adicionar campos ao grid
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descricaoField, 1, 1);
        grid.add(new Label("Tipo de Menu:"), 0, 2);
        grid.add(tipoMenuComboBox, 1, 2);
        grid.add(itemsPane, 0, 3, 2, 1);

        // Expandir a área de itens
        GridPane.setVgrow(itemsPane, Priority.ALWAYS);
        GridPane.setHgrow(itemsPane, Priority.ALWAYS);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> popup.hide());

        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("login-button");
        saveButton.setDisable(true);

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Add all components to popup content
        popupContent.getChildren().addAll(titleLabel, headerLabel, grid, buttonBox);

        popup.getContent().add(popupContent);

        // Focus on name field when opened
        Platform.runLater(() -> nomeField.requestFocus());

        // Validation
        nomeField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() ||
                    descricaoField.getText().trim().isEmpty() ||
                    tipoMenuComboBox.getValue() == null);
        });

        descricaoField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() ||
                    nomeField.getText().trim().isEmpty() ||
                    tipoMenuComboBox.getValue() == null);
        });

        tipoMenuComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nomeField.getText().trim().isEmpty() ||
                    descricaoField.getText().trim().isEmpty() ||
                    newValue == null);
        });

        // Expandir a área de itens quando clicada
        itemsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Aguardar um pouco antes de carregar os itens para dar tempo do loadMenuItems
                // completar
                Platform.runLater(() -> {
                    // Carregar itens da API quando expandir
                    loadItems(itemsContainer, selectedItemsIds);
                });
            }
        });

        // Save button action
        saveButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String descricao = descricaoField.getText().trim();
            TipoMenu tipoMenu = tipoMenuComboBox.getValue();

            if (tipoMenu != null) {
                // Criar o menu com os itens selecionados
                createMenu(tipoMenu.getId(), nome, descricao, selectedItemsIds);
                popup.hide();
            }
        });

        // Show popup centered
        popup.show(primary, centerX - 350, centerY - 350);
    }

    /**
     * Carrega os itens disponíveis da API e exibe para seleção (SÍNCRONO)
     * 
     * @param container        Container onde os itens serão exibidos
     * @param selectedItemsIds Lista observável para armazenar os IDs dos itens
     *                         selecionados
     */
    private void loadItems(VBox container, ObservableList<Integer> selectedItemsIds) {
        System.out.println("[MENU] Iniciando carregamento SÍNCRONO de itens disponíveis...");
        System.out.println("[MENU] IDs já selecionados: " + selectedItemsIds);

        HttpRequest getItemsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/getAll")))
                .GET()
                .build();

        try {
            // Requisição SÍNCRONA - aguarda resposta antes de continuar
            HttpResponse<String> resp = httpClient.send(getItemsReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta SÍNCRONA da API para itens - Status: " + resp.statusCode());
            if (resp.statusCode() == 200) {
                try {
                    System.out.println("[MENU] Resposta do servidor: " + resp.body());
                    List<Item> items = ItemJsonLoader.parseItems(resp.body());
                    System.out.println("[MENU] Itens parseados com sucesso. Total: " + items.size());

                    Platform.runLater(() -> {
                        container.getChildren().clear();

                        if (items.isEmpty()) {
                            Label noItems = new Label("Nenhum item disponível");
                            container.getChildren().add(noItems);
                            System.out.println("[MENU] Nenhum item disponível");
                            return;
                        }

                        Label headerLabel = new Label("Selecione os itens para incluir no menu:");
                        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                        // Criar lista de checkboxes para os itens
                        VBox checkBoxContainer = new VBox(5);
                        ScrollPane scrollPane = new ScrollPane(checkBoxContainer);
                        scrollPane.setFitToWidth(true);
                        scrollPane.setPrefHeight(400);

                        for (Item item : items) {
                            CheckBox cb = new CheckBox(item.getId() + " - " + item.getNome() + " - " +
                                    item.getTipoPratoName() + " (€" + item.getPreco() + ")");
                            cb.setUserData(item.getId());

                            // Pré-selecionar o checkbox se o item já estiver na lista de selecionados
                            boolean isPreSelected = selectedItemsIds.contains(item.getId());
                            cb.setSelected(isPreSelected);
                            System.out.println("[MENU] Item " + item.getNome() + " (ID: " + item.getId()
                                    + ") - Pré-selecionado: " + isPreSelected);

                            // Quando o checkbox é marcado/desmarcado, atualize a lista de IDs selecionados
                            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                                int itemId = (int) cb.getUserData();
                                System.out.println("[MENU] Mudança de seleção - Item ID: " + itemId
                                        + " - Novo estado: " + newVal);
                                if (newVal) {
                                    if (!selectedItemsIds.contains(itemId)) {
                                        selectedItemsIds.add(itemId);
                                        System.out
                                                .println("[MENU] Item adicionado à seleção. IDs selecionados: "
                                                        + selectedItemsIds);
                                    }
                                } else {
                                    selectedItemsIds.remove(Integer.valueOf(itemId));
                                    System.out.println("[MENU] Item removido da seleção. IDs selecionados: "
                                            + selectedItemsIds);
                                }
                            });

                            checkBoxContainer.getChildren().add(cb);
                        }

                        // Display selected count
                        Label selectionLabel = new Label(selectedItemsIds.size() + " item(s) selecionado(s)");

                        // Update selection counter when list changes
                        selectedItemsIds.addListener(
                                (javafx.collections.ListChangeListener.Change<? extends Integer> c) -> {
                                    selectionLabel.setText(selectedItemsIds.size() + " item(s) selecionado(s)");
                                    System.out.println("[MENU] Contador atualizado: " + selectedItemsIds.size()
                                            + " item(s) selecionado(s)");
                                });

                        // Layout
                        container.getChildren().addAll(headerLabel, scrollPane, selectionLabel);
                        System.out.println("[MENU] Interface de seleção de itens criada com sucesso");
                    });
                } catch (Exception ex) {
                    System.err.println("[MENU] Erro ao processar resposta dos itens: " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        Label errorLabel = new Label("Erro ao carregar itens: " + ex.getMessage());
                        errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                        container.getChildren().add(errorLabel);
                    });
                }
            } else {
                System.err.println("[MENU] Erro HTTP ao carregar itens. Status: " + resp.statusCode()
                        + ", Body: " + resp.body());
                Platform.runLater(() -> {
                    container.getChildren().clear();
                    Label errorLabel = new Label("Erro ao carregar itens. Status: " + resp.statusCode());
                    errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                    container.getChildren().add(errorLabel);
                });
            }
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA ao carregar itens: " + ex.getMessage());
            ex.printStackTrace();
            Platform.runLater(() -> {
                container.getChildren().clear();
                Label errorLabel = new Label("Erro ao carregar itens: " + ex.getMessage());
                errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                container.getChildren().add(errorLabel);
            });
        }
    }

    /**
     * Envia uma requisição para a API para criar um novo menu (SÍNCRONO)
     *
     * @param tipoMenuId ID do tipo de menu
     * @param nome       Nome do menu
     * @param descricao  Descrição do menu
     * @param itemsIds   Lista observável de IDs dos itens do menu
     */
    private void createMenu(int tipoMenuId, String nome, String descricao, ObservableList<Integer> itemsIds) {
        System.out.println("[MENU] Criando novo menu:");
        System.out.println("[MENU] - Tipo de Menu ID: " + tipoMenuId);
        System.out.println("[MENU] - Nome: " + nome);
        System.out.println("[MENU] - Descrição: " + descricao);
        System.out.println("[MENU] - IDs dos itens: " + itemsIds);

        // Construir o JSON para o corpo da requisição com formatação mais robusta
        StringBuilder itemsArray = new StringBuilder("[");
        for (int i = 0; i < itemsIds.size(); i++) {
            if (i > 0)
                itemsArray.append(", ");
            itemsArray.append(itemsIds.get(i));
        }
        itemsArray.append("]");

        String jsonBody = String.format(
                "{\"tipoMenuId\": %d, \"nome\": \"%s\", \"descricao\": \"%s\", \"itemsIds\": %s}",
                tipoMenuId,
                nome.replace("\"", "\\\""), // Escape quotes in name
                descricao.replace("\"", "\\\""), // Escape quotes in description
                itemsArray.toString());

        System.out.println("[MENU] JSON a ser enviado: " + jsonBody);

        // Criar a requisição HTTP
        HttpRequest createMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/create")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("[MENU] Enviando requisição para: " + AppConfig.getApiEndpoint("/menu/create"));

        try {
            // Requisição SÍNCRONA - aguarda resposta antes de continuar
            HttpResponse<String> resp = httpClient.send(createMenuReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta SÍNCRONA da criação do menu - Status: " + resp.statusCode());
            System.out.println("[MENU] Resposta SÍNCRONA da criação do menu - Body: " + resp.body());
            if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                Platform.runLater(() -> {
                    System.out.println("[MENU] Menu criado com sucesso!");
                    PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Menu criado com sucesso!",
                            "");

                    // Recarregar a view após criar o menu
                    show();
                });
            } else {
                System.err.println("[MENU] Falha ao criar menu. Status: " + resp.statusCode());
                showError("Falha ao criar menu", resp.statusCode());
            }
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA ao criar menu: " + ex.getMessage());
            ex.printStackTrace();
            showError("Falha ao criar menu", ex.getMessage());
        }
    }

    /**
     * Exclui um menu (SÍNCRONO)
     * 
     * @param id ID do menu a ser excluído
     */
    private void deleteMenu(int id) {
        System.out.println("[MENU] Iniciando exclusão SÍNCRONA do menu ID: " + id);

        // Criar a requisição HTTP
        HttpRequest deleteMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/delete?id=" + id)))
                .DELETE()
                .build();

        try {
            // Requisição SÍNCRONA - aguarda resposta antes de continuar
            HttpResponse<String> resp = httpClient.send(deleteMenuReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta SÍNCRONA da exclusão - Status: " + resp.statusCode());
            Platform.runLater(() -> {
                if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                    // Sucesso
                    System.out.println("[MENU] Menu excluído com sucesso!");
                    PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso", "Menu Excluído",
                            "O menu foi excluído com sucesso!");

                    // Recarregar a lista de menus
                    show();
                } else {
                    // Erro
                    System.err.println("[MENU] Falha ao excluir menu. Status: " + resp.statusCode());
                    PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir menu",
                            "Status code: " + resp.statusCode() + "\n\nResposta: " + resp.body());
                }
            });
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA ao excluir menu: " + ex.getMessage());
            ex.printStackTrace();

            Platform.runLater(() -> {
                PopUp.showPopupDialog(Alert.AlertType.ERROR, "Erro", "Falha ao excluir menu",
                        "Ocorreu um erro ao tentar enviar a solicitação: " + ex.getMessage());
            });
        }
    }

    /**
     * Exibe o diálogo para editar um menu existente
     * 
     * @param menu      Menu a ser editado
     * @param tiposMenu Lista de tipos de menu disponíveis
     */
    private void showEditMenuDialog(Menu menu, List<TipoMenu> tiposMenu) {
        // Get primary stage for positioning
        javafx.stage.Stage primary = StageManager.getPrimaryStage();
        double centerX = primary.getX() + primary.getWidth() / 2;
        double centerY = primary.getY() + primary.getHeight() / 2;

        // Create popup
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.setAutoHide(false); // Don't close automatically

        // Create content
        VBox popupContent = new VBox(15);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        popupContent.setPrefWidth(700);
        popupContent.setPrefHeight(700);

        // Title
        Label titleLabel = new Label("Editar Menu");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Header
        Label headerLabel = new Label("Editar detalhes do menu: " + menu.getNome());
        headerLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // Create form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Campo para nome do menu
        TextField nomeField = new TextField(menu.getNome());
        nomeField.setPromptText("Nome do menu");

        // Campo para descrição do menu
        TextArea descricaoField = new TextArea(menu.getDescricao());
        descricaoField.setPromptText("Descrição do menu");
        descricaoField.setPrefRowCount(3);
        descricaoField.setWrapText(true);

        // Dropdown para tipo de menu
        ComboBox<TipoMenu> tipoMenuComboBox = new ComboBox<>();
        tipoMenuComboBox.getItems().addAll(tiposMenu);

        // Selecionar o tipo de menu atual
        tipoMenuComboBox.getItems().stream()
                .filter(tipo -> tipo.getId() == menu.getTipoMenuId())
                .findFirst()
                .ifPresent(tipoMenuComboBox::setValue);

        tipoMenuComboBox.setCellFactory(lv -> new ListCell<TipoMenu>() {
            @Override
            protected void updateItem(TipoMenu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNome());
            }
        });
        tipoMenuComboBox.setButtonCell(new ListCell<TipoMenu>() {
            @Override
            protected void updateItem(TipoMenu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "Selecione o tipo de menu" : item.getNome());
            }
        });

        // Área de seleção de itens
        TitledPane itemsPane = new TitledPane();
        itemsPane.setText("Selecione os Itens para o Menu");
        itemsPane.setExpanded(false);
        itemsPane.setPrefHeight(400); // Make the pane larger

        VBox itemsContainer = new VBox(10);
        itemsContainer.setPadding(new Insets(10));

        Label loadingItemsLabel = new Label("Carregando itens disponíveis...");
        ProgressIndicator loadingProgress = new ProgressIndicator();
        loadingProgress.setPrefSize(20, 20);

        HBox loadingBox = new HBox(10, loadingProgress, loadingItemsLabel);
        loadingBox.setAlignment(Pos.CENTER_LEFT);

        itemsContainer.getChildren().add(loadingBox);
        itemsPane.setContent(itemsContainer);

        // Lista observável de IDs dos itens selecionados
        ObservableList<Integer> selectedItemsIds = FXCollections.observableArrayList();

        // Carregar os IDs dos itens do menu existente primeiro
        System.out.println("[MENU] Iniciando carregamento dos itens do menu para edição. Menu ID: " + menu.getId());
        loadMenuItems(menu.getId(), selectedItemsIds, () -> {
            // Apenas log após carregar os itens, sem expandir automaticamente
            Platform.runLater(() -> {
                System.out.println("[MENU] Callback executado, itens carregados");
                System.out.println("[MENU] IDs carregados no callback: " + selectedItemsIds);
            });
        });

        // Adicionar campos ao grid
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descricaoField, 1, 1);
        grid.add(new Label("Tipo de Menu:"), 0, 2);
        grid.add(tipoMenuComboBox, 1, 2);
        grid.add(itemsPane, 0, 3, 2, 1);

        // Expandir a área de itens
        GridPane.setVgrow(itemsPane, Priority.ALWAYS);
        GridPane.setHgrow(itemsPane, Priority.ALWAYS);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> popup.hide());

        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("login-button");
        saveButton.setDisable(false); // Não desabilitar inicialmente porque já temos valores válidos

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Add all components to popup content
        popupContent.getChildren().addAll(titleLabel, headerLabel, grid, buttonBox);

        popup.getContent().add(popupContent);

        // Focus on name field when opened
        Platform.runLater(() -> {
            nomeField.requestFocus();
            // Expandir os itens automaticamente para mostrar os itens selecionados
        });

        // Validation
        nomeField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() ||
                    descricaoField.getText().trim().isEmpty() ||
                    tipoMenuComboBox.getValue() == null);
        });

        descricaoField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() ||
                    nomeField.getText().trim().isEmpty() ||
                    tipoMenuComboBox.getValue() == null);
        });

        tipoMenuComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nomeField.getText().trim().isEmpty() ||
                    descricaoField.getText().trim().isEmpty() ||
                    newValue == null);
        });

        // Expandir a área de itens quando clicada
        itemsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Aguardar um pouco antes de carregar os itens para dar tempo do loadMenuItems
                // completar
                Platform.runLater(() -> {
                    // Carregar itens da API quando expandir
                    loadItems(itemsContainer, selectedItemsIds);
                });
            }
        });

        // Save button action
        saveButton.setOnAction(e -> {
            String nome = nomeField.getText().trim();
            String descricao = descricaoField.getText().trim();
            TipoMenu tipoMenu = tipoMenuComboBox.getValue();

            if (tipoMenu != null) {
                // Atualizar o menu com os itens selecionados
                updateMenu(menu.getId(), tipoMenu.getId(), nome, descricao, selectedItemsIds);
                popup.hide();
            }
        });

        // Show popup centered
        popup.show(primary, centerX - 350, centerY - 350);
        new Thread(() -> {
            try {
                Thread.sleep(200);
                Platform.runLater(() -> itemsPane.setExpanded(true));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrompida: " + ex.getMessage());
            }
        }).start();
    }

    /**
     * Carrega os IDs dos itens de um menu existente (SÍNCRONO)
     * 
     * @param menuId           ID do menu
     * @param selectedItemsIds Lista observável para armazenar os IDs dos itens
     *                         selecionados
     * @param onComplete       Callback executado quando o carregamento termina
     */
    private void loadMenuItems(int menuId, ObservableList<Integer> selectedItemsIds, Runnable onComplete) {
        System.out.println("[MENU] Iniciando carregamento SÍNCRONO dos itens do menu ID: " + menuId);
        System.out.println("[MENU] Lista de IDs atual antes do carregamento: " + selectedItemsIds);

        HttpRequest getItemsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/getMenuItens?id=" + menuId)))
                .GET()
                .build();

        try {
            // Requisição SÍNCRONA - aguarda resposta antes de continuar
            HttpResponse<String> resp = httpClient.send(getItemsReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta SÍNCRONA do loadMenuItems - Status: " + resp.statusCode());
            if (resp.statusCode() == 200) {
                try {
                    System.out.println("[MENU] Resposta recebida do loadMenuItems: " + resp.body());
                    List<Integer> itemIds = new ArrayList<Integer>();
                    List<Item> items = new ArrayList<>();
                    try {
                        items = ItemJsonLoader.parseItems(resp.body());
                        System.out.println("[MENU] Itens parseados no loadMenuItems. Total: " + items.size());
                    } catch (Exception e) {
                        System.err.println("[MENU] Erro ao parsear itens no loadMenuItems: " + e.getMessage());
                        e.printStackTrace();
                    }
                    for (Item item : items) {
                        itemIds.add(item.getId());
                        System.out.println(
                                "[MENU] Item adicionado à lista: " + item.getId() + " - " + item.getNome());
                    }

                    System.out.println("[MENU] IDs dos itens obtidos: " + itemIds);
                    Platform.runLater(() -> {
                        selectedItemsIds.clear(); // Limpar a lista antes de adicionar
                        selectedItemsIds.addAll(itemIds); // Usar addAll em vez de setAll
                        System.out.println(
                                "[MENU] IDs dos itens adicionados à lista observável: " + selectedItemsIds);

                        // Executar callback quando terminar
                        if (onComplete != null) {
                            System.out.println("[MENU] Executando callback onComplete");
                            onComplete.run();
                        } else {
                            System.out.println("[MENU] Callback onComplete é null");
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("[MENU] Erro ao carregar itens do menu: " + ex.getMessage());
                    ex.printStackTrace();
                    // Se houver erro, ainda executar o callback
                    Platform.runLater(() -> {
                        if (onComplete != null) {
                            System.out.println("[MENU] Executando callback onComplete após erro");
                            onComplete.run();
                        }
                    });
                }
            } else {
                System.err.println("[MENU] Erro HTTP no loadMenuItems. Status: " + resp.statusCode()
                        + ", Body: " + resp.body());
                Platform.runLater(() -> {
                    if (onComplete != null) {
                        System.out.println("[MENU] Executando callback onComplete após erro HTTP");
                        onComplete.run();
                    }
                });
            }
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA no loadMenuItems: " + ex.getMessage());
            ex.printStackTrace();
            Platform.runLater(() -> {
                if (onComplete != null) {
                    System.out.println("[MENU] Executando callback onComplete após exceção");
                    onComplete.run();
                }
            });
        }
    }

    /**
     * Atualiza um menu existente (SÍNCRONO)
     * 
     * @param id         ID do menu a ser atualizado
     * @param tipoMenuId ID do tipo de menu
     * @param nome       Nome do menu
     * @param descricao  Descrição do menu
     * @param itemsIds   Lista de IDs dos itens do menu
     */
    private void updateMenu(int id, int tipoMenuId, String nome, String descricao, ObservableList<Integer> itemsIds) {
        System.out.println("[MENU] Atualizando menu:");
        System.out.println("[MENU] - ID do menu: " + id);
        System.out.println("[MENU] - Tipo de Menu ID: " + tipoMenuId);
        System.out.println("[MENU] - Nome: " + nome);
        System.out.println("[MENU] - Descrição: " + descricao);
        System.out.println("[MENU] - IDs dos itens: " + itemsIds);

        // Construir o JSON para o corpo da requisição
        String jsonBody = String.format(
                "{\"tipoMenuId\": %d, \"nome\": \"%s\", \"descricao\": \"%s\", \"itemsIds\": %s}",
                tipoMenuId,
                nome,
                descricao,
                itemsIds.isEmpty() ? "[]" : itemsIds.toString());

        System.out.println("[MENU] JSON a ser enviado para atualização: " + jsonBody);

        // Criar a requisição HTTP
        HttpRequest updateMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/update?id=" + id)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println(
                "[MENU] Enviando requisição de atualização para: " + AppConfig.getApiEndpoint("/menu/update?id=" + id));

        try {
            // Requisição SÍNCRONA - aguarda resposta antes de continuar
            HttpResponse<String> resp = httpClient.send(updateMenuReq, HttpResponse.BodyHandlers.ofString());

            System.out.println("[MENU] Resposta SÍNCRONA da atualização do menu - Status: " + resp.statusCode());
            System.out.println("[MENU] Resposta SÍNCRONA da atualização do menu - Body: " + resp.body());
            if (resp.statusCode() == 200) {
                Platform.runLater(() -> {
                    System.out.println("[MENU] Menu atualizado com sucesso!");
                    PopUp.showPopupDialog(Alert.AlertType.INFORMATION, "Sucesso",
                            "Menu atualizado com sucesso!", "");

                    // Recarregar a view após atualizar o menu
                    show();
                });
            } else {
                System.err.println("[MENU] Falha ao atualizar menu. Status: " + resp.statusCode());
                showError("Falha ao atualizar menu", resp.statusCode());
            }
        } catch (Exception ex) {
            System.err.println("[MENU] Exceção SÍNCRONA ao atualizar menu: " + ex.getMessage());
            ex.printStackTrace();
            showError("Falha ao atualizar menu", ex.getMessage());
        }
    }
}
