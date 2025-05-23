package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.EatEaseFrontend.JsonParser;
import com.EatEaseFrontend.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * View para gerenciar e exibir pedidos do restaurante
 */
public class PedidosView {

    private final StackPane contentArea;
    private final HttpClient httpClient;

    // Temporizador para atualizações automáticas
    private Timer autoUpdateTimer;
    // Intervalo de atualização em segundos
    private final int updateIntervalSeconds = 15;
    // Flag para controlar se a view está ativa
    private boolean isViewActive = false;

    /**
     * Construtor da view de pedidos
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public PedidosView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
    }

    /**
     * Carrega e exibe a lista de pedidos
     */
    public void show() {
        System.out.println("Carregando lista de pedidos...");

        // Marcar a view como ativa
        isViewActive = true;

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando pedidos...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get pedidos
        loadPedidos();

        // Iniciar o temporizador de atualização automática se ainda não estiver ativo
        startAutoUpdateTimer();
    }

    /**
     * Carrega os pedidos do servidor
     */
    private void loadPedidos() {
        HttpRequest getPedidosReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/pedido/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getPedidosReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("Pedidos recebidos: " + response.body());
                        return JsonParser.parsePedidos(response.body());
                    } else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar pedidos");
                            alert.setContentText("Erro: Código " + response.statusCode());
                            alert.showAndWait();
                        });
                        return List.<Pedido>of();
                    }
                })
                .thenAccept(pedidos -> {
                    if (!isViewActive) {
                        return; // Se a view não está mais ativa, não atualize a UI
                    }
                    Platform.runLater(() -> displayPedidosAsCards(pedidos));
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao carregar pedidos");
                        alert.setContentText("Erro: " + e.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Inicia o temporizador para atualização automática dos pedidos
     */
    private void startAutoUpdateTimer() {
        // Cancelar qualquer temporizador existente
        stopAutoUpdateTimer();

        // Criar um novo temporizador
        autoUpdateTimer = new Timer();
        autoUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isViewActive) {
                    silentlyUpdatePedidos();
                }
            }
        }, updateIntervalSeconds * 1000, updateIntervalSeconds * 1000);
    }

    /**
     * Para o temporizador de atualização automática
     */
    private void stopAutoUpdateTimer() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    /**
     * Atualiza os pedidos silenciosamente (sem mostrar indicador de carregamento)
     */
    private void silentlyUpdatePedidos() {
        // Verificar se a view está ativa antes de iniciar a atualização
        if (!isViewActive) {
            System.out.println("View de pedidos não está ativa, ignorando atualização automática");
            stopAutoUpdateTimer(); // Garantir que o timer seja parado se a view não está ativa
            return;
        }

        System.out.println("Realizando atualização automática dos pedidos...");
        HttpRequest getPedidosReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/pedido/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getPedidosReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return JsonParser.parsePedidos(response.body());
                    } else {
                        System.err.println("Erro ao atualizar pedidos: " + response.statusCode());
                        return List.<Pedido>of();
                    }
                })
                .thenAccept(pedidos -> {
                    if (!isViewActive) {
                        stopAutoUpdateTimer(); // Parar o timer se a view não está ativa
                        return;
                    }
                    if (!pedidos.isEmpty()) {
                        Platform.runLater(() -> {
                            // Preservar a posição de rolagem atual antes da atualização
                            double scrollPosition = 0;
                            for (Node node : contentArea.getChildren()) {
                                if (node instanceof VBox) {
                                    VBox mainLayout = (VBox) node;
                                    for (Node child : mainLayout.getChildren()) {
                                        if (child instanceof ScrollPane) {
                                            scrollPosition = ((ScrollPane) child).getVvalue();
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }

                            // Atualizar os cards sem rolar para o topo
                            displayPedidosAsCards(pedidos);

                            // Restaurar a posição de rolagem após a atualização
                            final double finalScrollPos = scrollPosition;
                            Platform.runLater(() -> {
                                for (Node node : contentArea.getChildren()) {
                                    if (node instanceof VBox) {
                                        VBox mainLayout = (VBox) node;
                                        for (Node child : mainLayout.getChildren()) {
                                            if (child instanceof ScrollPane) {
                                                ((ScrollPane) child).setVvalue(finalScrollPos);
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            });
                        });
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Exceção ao atualizar pedidos: " + e.getMessage());
                    return null;
                });
    }

    /**
     * Exibe os pedidos como cards em um ScrollPane
     * 
     * @param pedidos Lista de pedidos a serem exibidos
     */
    private void displayPedidosAsCards(List<Pedido> pedidos) {
        contentArea.getChildren().clear();

        // Header with title and refresh button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Text title = new Text("Gestão de Pedidos");
        title.getStyleClass().add("welcome-text");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        Button refreshButton = new Button();
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.valueOf("#2a5298"));
        refreshIcon.setIconSize(20);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setTooltip(new Tooltip("Atualizar pedidos"));
        refreshButton.setOnAction(e -> show());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, refreshButton);

        // Container para os cards de pedidos
        VBox pedidosContainer = new VBox(20);
        pedidosContainer.setPadding(new Insets(20));

        if (pedidos.isEmpty()) {
            Text noPedidosText = new Text("Não há pedidos disponíveis");
            noPedidosText.getStyleClass().add("welcome-text");
            pedidosContainer.getChildren().add(noPedidosText);
        } else {
            // Ordenar os pedidos por ID em ordem decrescente (mais novos primeiro)
            pedidos.sort(Comparator.comparing(Pedido::getId).reversed());

            // Adicione cada pedido como um card
            for (Pedido pedido : pedidos) {
                pedidosContainer.getChildren().add(createPedidoCard(pedido));
            }
        }

        // Add to scroll pane
        ScrollPane scrollPane = new ScrollPane(pedidosContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(header, scrollPane);

        contentArea.getChildren().add(mainLayout);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Item getItemById(int id) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/getByPratoId?pratoId=" + id)))
                .GET()
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        return MAPPER.readValue(resp.body(), Item.class);
    }

    /**
     * Atualiza o estado de um pedido
     * 
     * @param pedidoId       ID do pedido a atualizar
     * @param estadoPedidoId Novo estado do pedido (5=Pendente, 1=Em preparo,
     *                       2=Pronto, 3=Entregue, 4=Cancelado)
     */
    private void atualizarEstadoPedido(int pedidoId, int estadoPedidoId) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig
                        .getApiEndpoint("/pedido/setEstado?id=" + pedidoId + "&estadoPedido_id=" + estadoPedidoId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("Estado do pedido #" + pedidoId + " atualizado para " + estadoPedidoId);
                        // Recarregar pedidos após atualização bem-sucedida
                        Platform.runLater(this::show);
                        return true;
                    } else {
                        System.err.println("Erro ao atualizar estado do pedido: " + response.statusCode());
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao atualizar estado do pedido");
                            alert.setContentText("Erro: Código " + response.statusCode());
                            alert.showAndWait();
                        });
                        return false;
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText("Falha ao atualizar estado do pedido");
                        alert.setContentText("Erro: " + e.getMessage());
                        alert.showAndWait();
                    });
                    return false;
                });
    }

    /**
     * Cria um card para um pedido
     * 
     * @param pedido Pedido para o qual criar o card
     * @return VBox contendo o card do pedido
     */
    private VBox createPedidoCard(Pedido pedido) {
        VBox card = new VBox(12);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(15));

        // Cabeçalho do card com ID do pedido e data/hora
        HBox cardHeader = new HBox(15);
        cardHeader.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Pedido #" + pedido.getId());
        idLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        idLabel.getStyleClass().add("card-title");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label dateLabel = new Label("Data: " + pedido.getDataHoraFormatada());
        dateLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        cardHeader.getChildren().addAll(idLabel, headerSpacer, dateLabel);

        // Informações do pedido
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(10, 0, 15, 0));

        // Adicionar informações ao grid
        infoGrid.add(new Label("Mesa:"), 0, 0);
        infoGrid.add(new Label("#" + pedido.getMesa_id()), 1, 0);

        infoGrid.add(new Label("Funcionário:"), 0, 1);
        infoGrid.add(new Label("#" + pedido.getFuncionario_id()), 1, 1);

        // Estado do pedido com badge colorida
        HBox estadoBox = new HBox(10);
        estadoBox.setAlignment(Pos.CENTER_LEFT);

        Label estadoText = new Label("Estado:");

        Label estadoLabel = new Label(pedido.getEstadoNome());
        estadoLabel.setPadding(new Insets(5, 10, 5, 10));
        estadoLabel.setTextFill(Color.WHITE);

        // Definir cor baseada no estado
        Background estadoBg;
        switch (pedido.getEstadoPedido_id()) {
            case 1: // Em preparo
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#f0ad4e"), new CornerRadii(4), Insets.EMPTY));
                break;
            case 2: // Pronto
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#5cb85c"), new CornerRadii(4), Insets.EMPTY));
                break;
            case 3: // Entregue
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#5bc0de"), new CornerRadii(4), Insets.EMPTY));
                break;
            case 4: // Cancelado
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#d9534f"), new CornerRadii(4), Insets.EMPTY));
                break;
            case 5: // Pendente
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#777777"), new CornerRadii(4), Insets.EMPTY));
                break;
            default:
                estadoBg = new Background(new BackgroundFill(
                        Color.web("#777777"), new CornerRadii(4), Insets.EMPTY));
        }
        estadoLabel.setBackground(estadoBg);

        estadoBox.getChildren().addAll(estadoText, estadoLabel);

        // Adicionar botões para alterar o estado do pedido
        HBox botoesEstadoBox = new HBox(10);
        botoesEstadoBox.setAlignment(Pos.CENTER_LEFT);
        botoesEstadoBox.setPadding(new Insets(10, 0, 0, 0));

        Label botoesEstadoLabel = new Label("Alterar Estado:");
        botoesEstadoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Botão para estado Pendente (5)
        Button btnPendente = new Button("Pendente");
        btnPendente.getStyleClass().add("icon-button");
        btnPendente.setStyle("-fx-background-color: #777777; -fx-text-fill: white;");
        btnPendente.setOnAction(e -> atualizarEstadoPedido(pedido.getId(), 5));

        // Botão para estado Em Preparo (1)
        Button btnEmPreparo = new Button("Em Preparo");
        btnEmPreparo.getStyleClass().add("icon-button");
        btnEmPreparo.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");
        btnEmPreparo.setOnAction(e -> atualizarEstadoPedido(pedido.getId(), 1));

        // Botão para estado Pronto (2)
        Button btnPronto = new Button("Pronto");
        btnPronto.getStyleClass().add("icon-button");
        btnPronto.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
        btnPronto.setOnAction(e -> atualizarEstadoPedido(pedido.getId(), 2));

        // Botão para estado Entregue (3)
        Button btnEntregue = new Button("Entregue");
        btnEntregue.getStyleClass().add("icon-button");
        btnEntregue.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        btnEntregue.setOnAction(e -> atualizarEstadoPedido(pedido.getId(), 3));

        // Botão para estado Cancelado (4)
        Button btnCancelado = new Button("Cancelado");
        btnCancelado.getStyleClass().add("icon-button");
        btnCancelado.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        btnCancelado.setOnAction(e -> atualizarEstadoPedido(pedido.getId(), 4));

        // Adicionar os botões ao container
        VBox estadoBotoesContainer = new VBox(10);
        estadoBotoesContainer.getChildren().add(botoesEstadoLabel);

        // Criar FlowPane para os botões para melhor responsividade
        FlowPane botoesFlow = new FlowPane(10, 10);
        botoesFlow.getChildren().addAll(btnPendente, btnEmPreparo, btnPronto, btnEntregue, btnCancelado);
        estadoBotoesContainer.getChildren().add(botoesFlow);

        // Desativar o botão do estado atual
        switch (pedido.getEstadoPedido_id()) {
            case 1:
                btnEmPreparo.setDisable(true);
                break;
            case 2:
                btnPronto.setDisable(true);
                break;
            case 3:
                btnEntregue.setDisable(true);
                break;
            case 4:
                btnCancelado.setDisable(true);
                break;
            case 5:
                btnPendente.setDisable(true);
                break;
        }

        // Itens do pedido
        TitledPane itemsPane = new TitledPane();
        itemsPane.setText("Itens do Pedido");

        VBox itemsContainer = new VBox(8);
        itemsContainer.setPadding(new Insets(10));

        if (pedido.getItensIds().isEmpty()) {
            itemsContainer.getChildren().add(new Label("Nenhum item no pedido"));
        } else {
            int i = 0;
            for (Integer itemId : pedido.getItensIds()) {
                try {
                    Item item = getItemById(itemId);
                    if (item == null) {
                        itemsContainer.getChildren().add(new Label("Item #" + itemId + " não encontrado"));
                        continue;
                    }

                    HBox itemBox = new HBox(8);
                    itemBox.setAlignment(Pos.CENTER_LEFT);

                    FontIcon itemIcon = new FontIcon(MaterialDesign.MDI_FOOD);
                    itemIcon.setIconColor(Color.valueOf("#2a5298"));

                    Label itemLabel = new Label("Item " + i + " " + item.getNome());

                    itemBox.getChildren().addAll(itemIcon, itemLabel);
                    itemsContainer.getChildren().add(itemBox);
                } catch (Exception e) {
                    System.err.println("Erro ao carregar item #" + itemId + ": " + e.getMessage());
                    itemsContainer.getChildren().add(new Label("Erro ao carregar item #" + itemId));
                }
                i++;
            }
        }

        itemsPane.setContent(itemsContainer);
        itemsPane.setExpanded(true);

        // Observação
        VBox observacaoBox = new VBox(5);
        Label obsTitle = new Label("Observação:");
        obsTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label obsContent = new Label(pedido.getObservacao().isEmpty() ? "Sem observações" : pedido.getObservacao());
        obsContent.setWrapText(true);

        observacaoBox.getChildren().addAll(obsTitle, obsContent);

        // Ingredientes a remover
        VBox ingredientesBox = new VBox(5);
        Label ingredTitle = new Label("Ingredientes a Remover:");
        ingredTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        VBox ingredContent = new VBox(3);
        if (pedido.getIngredientesRemover().isEmpty()) {
            ingredContent.getChildren().add(new Label("Nenhum ingrediente a remover"));
        } else {
            for (Integer ingredId : pedido.getIngredientesRemover()) {
                try {
                    Item item = getItemById(ingredId);
                    if (item == null) {
                        ingredContent.getChildren().add(new Label("Ingrediente #" + ingredId + " não encontrado"));
                        continue;
                    }
                    ingredContent.getChildren().add(new Label("Ingrediente " + item.getNome()));
                } catch (Exception e) {
                    System.err.println("Erro ao carregar ingrediente #" + ingredId + ": " + e.getMessage());
                    ingredContent.getChildren().add(new Label("Erro ao carregar ingrediente #" + ingredId));
                }
            }
        }

        ingredientesBox.getChildren().addAll(ingredTitle, ingredContent);

        // Adicionar elementos ao card
        card.getChildren().addAll(
                cardHeader,
                new Separator(),
                infoGrid,
                estadoBox,
                estadoBotoesContainer,
                itemsPane,
                observacaoBox,
                ingredientesBox);

        return card;
    }

    /**
     * Método para limpar recursos quando a view não está mais visível
     */
    public void dispose() {
        isViewActive = false;
        stopAutoUpdateTimer();
        System.out.println("Limpeza de recursos da view de pedidos");
    }
}
