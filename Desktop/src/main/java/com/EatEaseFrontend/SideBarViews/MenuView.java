package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.Menu;
import com.EatEaseFrontend.TipoMenu;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.Map;
import java.util.Optional;
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
     * Carrega e exibe a lista de menus
     */
    public void show() {
        System.out.println("Carregando lista de menus...");

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

        httpClient.sendAsync(reqMenus, HttpResponse.BodyHandlers.ofString())
                .thenAccept(respMenus -> {
                    if (respMenus.statusCode() == 200) {
                        List<Menu> menus = JsonParser.parseMenus(respMenus.body());
                        httpClient.sendAsync(reqTipos, HttpResponse.BodyHandlers.ofString())
                                .thenAccept(respTipos -> {
                                    if (respTipos.statusCode() == 200) {
                                        List<TipoMenu> tipos = JsonParser.parseTipoMenus(respTipos.body());
                                        Platform.runLater(() -> displayMenusAsCards(menus, tipos));
                                    } else {
                                        showError("Falha ao carregar tipos de menu", respTipos.statusCode());
                                    }
                                })
                                .exceptionally(ex -> {
                                    showError("Falha ao carregar tipos de menu", ex.getMessage());
                                    return null;
                                });
                    } else {
                        showError("Falha ao carregar menus", respMenus.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    showError("Falha ao carregar menus", ex.getMessage());
                    return null;
                });
    }

    private void showError(String header, int status) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(header);
            alert.setContentText("Status code: " + status);
            alert.showAndWait();
        });
    }

    private void showError(String header, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(header);
            alert.setContentText("Erro: " + message);
            alert.showAndWait();
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
                    confirmDeleteMenu(m);
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
        // Criar o diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Menu");
        dialog.setHeaderText("Preencha os detalhes do novo menu");

        // Configurar botões
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Criar o grid para o formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

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

        // Configurar o tamanho do diálogo
        dialog.getDialogPane().setPrefSize(600, 500);

        // Configurar o conteúdo do diálogo
        dialog.getDialogPane().setContent(grid);

        // Focar no campo de nome ao abrir o diálogo
        Platform.runLater(() -> nomeField.requestFocus());

        // Validar os campos antes de habilitar o botão de salvar
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validar quando os campos mudam
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
            if (newValue && loadingItemsLabel.isVisible()) {
                // Carregar itens da API quando expandir pela primeira vez
                loadItems(itemsContainer, selectedItemsIds);
            }
        });

        // Processar o resultado
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String nome = nomeField.getText().trim();
            String descricao = descricaoField.getText().trim();
            TipoMenu tipoMenu = tipoMenuComboBox.getValue();

            if (tipoMenu != null) {
                // Criar o menu com os itens selecionados
                createMenu(tipoMenu.getId(), nome, descricao, selectedItemsIds);
            }
        }
    }

    /**
     * Carrega os itens disponíveis da API e exibe para seleção
     * 
     * @param container        Container onde os itens serão exibidos
     * @param selectedItemsIds Lista observável para armazenar os IDs dos itens
     *                         selecionados
     */
    private void loadItems(VBox container, ObservableList<Integer> selectedItemsIds) {
        HttpRequest getItemsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getItemsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        try {
                            List<Item> items = ItemJsonLoader.parseItems(resp.body());

                            Platform.runLater(() -> {
                                container.getChildren().clear();

                                if (items.isEmpty()) {
                                    Label noItems = new Label("Nenhum item disponível");
                                    container.getChildren().add(noItems);
                                    return;
                                }

                                Label headerLabel = new Label("Selecione os itens para incluir no menu:");
                                headerLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                                // Criar lista de checkboxes para os itens
                                VBox checkBoxContainer = new VBox(5);
                                ScrollPane scrollPane = new ScrollPane(checkBoxContainer);
                                scrollPane.setFitToWidth(true);
                                scrollPane.setPrefHeight(200);

                                for (Item item : items) {
                                    CheckBox cb = new CheckBox(item.getId() + " - " + item.getNome() + " - " +
                                            item.getTipoPratoName() + " (€" + item.getPreco() + ")");
                                    cb.setUserData(item.getId());

                                    // Quando o checkbox é marcado/desmarcado, atualize a lista de IDs selecionados
                                    cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                                        int itemId = (int) cb.getUserData();
                                        if (newVal) {
                                            if (!selectedItemsIds.contains(itemId)) {
                                                selectedItemsIds.add(itemId);
                                            }
                                        } else {
                                            selectedItemsIds.remove(Integer.valueOf(itemId));
                                        }
                                    });

                                    checkBoxContainer.getChildren().add(cb);
                                }

                                // Display selected count
                                Label selectionLabel = new Label("0 item(s) selecionado(s)");

                                // Update selection counter when list changes
                                selectedItemsIds.addListener(
                                        (javafx.collections.ListChangeListener.Change<? extends Integer> c) -> {
                                            selectionLabel.setText(selectedItemsIds.size() + " item(s) selecionado(s)");
                                        });

                                // Layout
                                container.getChildren().addAll(headerLabel, scrollPane, selectionLabel);
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                container.getChildren().clear();
                                Label errorLabel = new Label("Erro ao carregar itens: " + ex.getMessage());
                                errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                                container.getChildren().add(errorLabel);
                            });
                        }
                    } else {
                        Platform.runLater(() -> {
                            container.getChildren().clear();
                            Label errorLabel = new Label("Erro ao carregar itens. Status: " + resp.statusCode());
                            errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                            container.getChildren().add(errorLabel);
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        container.getChildren().clear();
                        Label errorLabel = new Label("Erro ao carregar itens: " + ex.getMessage());
                        errorLabel.setTextFill(javafx.scene.paint.Color.RED);
                        container.getChildren().add(errorLabel);
                    });
                    return null;
                });
    }

    /**
     * Envia uma requisição para a API para criar um novo menu
     *
     * @param tipoMenuId ID do tipo de menu
     * @param nome       Nome do menu
     * @param descricao  Descrição do menu
     * @param itemsIds   Lista observável de IDs dos itens do menu
     */
    private void createMenu(int tipoMenuId, String nome, String descricao, ObservableList<Integer> itemsIds) {
        // Construir o JSON para o corpo da requisição
        String jsonBody = String.format(
                "{\"tipoMenuId\": %d, \"nome\": \"%s\", \"descricao\": \"%s\", \"itemsIds\": %s}",
                tipoMenuId,
                nome,
                descricao,
                itemsIds.isEmpty() ? "[]" : itemsIds.toString());

        // Criar a requisição HTTP
        HttpRequest createMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/create")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição
        httpClient.sendAsync(createMenuReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200 || resp.statusCode() == 201) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText("Menu criado com sucesso!");
                            alert.showAndWait();

                            // Recarregar a view após criar o menu
                            show();
                        });
                    } else {
                        showError("Falha ao criar menu", resp.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    showError("Falha ao criar menu", ex.getMessage());
                    return null;
                });
    }

    /**
     * Confirma a exclusão de um menu
     * 
     * @param menu Menu a ser excluído
     */
    private void confirmDeleteMenu(Menu menu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Menu: " + menu.getNome());
        alert.setContentText("Tem certeza que deseja excluir este menu? Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteMenu(menu.getId());
        }
    }

    /**
     * Exclui um menu
     * 
     * @param id ID do menu a ser excluído
     */
    private void deleteMenu(int id) {
        // Criar a requisição HTTP
        HttpRequest deleteMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/delete?id=" + id)))
                .DELETE()
                .build();

        // Enviar a requisição de forma assíncrona
        httpClient.sendAsync(deleteMenuReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    Platform.runLater(() -> {
                        if (resp.statusCode() == 200 || resp.statusCode() == 204) {
                            // Sucesso
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Sucesso");
                            successAlert.setHeaderText("Menu Excluído");
                            successAlert.setContentText("O menu foi excluído com sucesso!");

                            successAlert.showAndWait();

                            // Recarregar a lista de menus
                            show();
                        } else {
                            // Erro
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erro");
                            errorAlert.setHeaderText("Falha ao excluir menu");
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
                        errorAlert.setHeaderText("Falha ao excluir menu");
                        errorAlert.setContentText("Ocorreu um erro ao tentar enviar a solicitação: " + ex.getMessage());

                        errorAlert.showAndWait();
                    });

                    return null;
                });
    }

    /**
     * Exibe o diálogo para editar um menu existente
     * 
     * @param menu      Menu a ser editado
     * @param tiposMenu Lista de tipos de menu disponíveis
     */
    private void showEditMenuDialog(Menu menu, List<TipoMenu> tiposMenu) {
        // Criar o diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Menu");
        dialog.setHeaderText("Editar detalhes do menu: " + menu.getNome());

        // Configurar botões
        ButtonType saveButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Criar o grid para o formulário
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

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

        // Carregar os IDs dos itens do menu existente
        loadMenuItems(menu.getId(), selectedItemsIds);

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

        // Configurar o tamanho do diálogo
        dialog.getDialogPane().setPrefSize(600, 500);

        // Configurar o conteúdo do diálogo
        dialog.getDialogPane().setContent(grid);

        // Focar no campo de nome ao abrir o diálogo
        Platform.runLater(() -> nomeField.requestFocus());

        // Validar os campos antes de habilitar o botão de salvar
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false); // Não desabilitar inicialmente porque já temos valores válidos

        // Validar quando os campos mudam
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
            if (newValue && loadingItemsLabel.isVisible()) {
                // Carregar itens da API quando expandir pela primeira vez
                loadItems(itemsContainer, selectedItemsIds);
            }
        });

        // Processar o resultado
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            String nome = nomeField.getText().trim();
            String descricao = descricaoField.getText().trim();
            TipoMenu tipoMenu = tipoMenuComboBox.getValue();

            if (tipoMenu != null) {
                // Atualizar o menu com os itens selecionados
                updateMenu(menu.getId(), tipoMenu.getId(), nome, descricao, selectedItemsIds);
            }
        }
    }

    /**
     * Carrega os IDs dos itens de um menu existente
     * 
     * @param menuId           ID do menu
     * @param selectedItemsIds Lista observável para armazenar os IDs dos itens
     *                         selecionados
     */
    private void loadMenuItems(int menuId, ObservableList<Integer> selectedItemsIds) {
        HttpRequest getItemsReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/getItemIds?menuId=" + menuId)))
                .GET()
                .build();

        httpClient.sendAsync(getItemsReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        try {
                            List<Integer> itemIds = JsonParser.parseMenuItemIds(resp.body());
                            Platform.runLater(() -> {
                                selectedItemsIds.setAll(itemIds);
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            // Se houver erro, simplesmente não carrega os IDs existentes
                        }
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Atualiza um menu existente
     * 
     * @param id         ID do menu a ser atualizado
     * @param tipoMenuId ID do tipo de menu
     * @param nome       Nome do menu
     * @param descricao  Descrição do menu
     * @param itemsIds   Lista de IDs dos itens do menu
     */
    private void updateMenu(int id, int tipoMenuId, String nome, String descricao, ObservableList<Integer> itemsIds) {
        // Construir o JSON para o corpo da requisição
        String jsonBody = String.format(
                "{\"tipoMenuId\": %d, \"nome\": \"%s\", \"descricao\": \"%s\", \"itemsIds\": %s}",
                tipoMenuId,
                nome,
                descricao,
                itemsIds.isEmpty() ? "[]" : itemsIds.toString());

        // Criar a requisição HTTP
        HttpRequest updateMenuReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/menu/update?id=" + id)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Enviar a requisição
        httpClient.sendAsync(updateMenuReq, HttpResponse.BodyHandlers.ofString())
                .thenAccept(resp -> {
                    if (resp.statusCode() == 200) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Sucesso");
                            alert.setHeaderText("Menu atualizado com sucesso!");
                            alert.showAndWait();

                            // Recarregar a view após atualizar o menu
                            show();
                        });
                    } else {
                        showError("Falha ao atualizar menu", resp.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    showError("Falha ao atualizar menu", ex.getMessage());
                    return null;
                });
    }
}
