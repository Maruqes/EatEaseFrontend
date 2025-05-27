package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard view that displays key restaurant metrics and statistics
 */
public class DashboardView {
    private final StackPane contentArea;
    private final HttpClient httpClient;
    private VBox mainContainer;
    private GridPane metricsGrid;
    private Label lastUpdatedLabel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("de", "DE"));

    // Parâmetros para dados de itens mais vendidos
    private static final int DEFAULT_DAYS = 30;
    private static final int TOP_ITEMS_COUNT = 3;
    private GridPane itemsGrid;

    // Componentes do gráfico de vendas
    private LineChart<Number, Number> salesChart;
    private NumberAxis salesXAxis;
    private NumberAxis salesYAxis;
    private HBox chartControlsContainer;
    private ToggleGroup periodToggleGroup;
    private DatePicker customStartDate;
    private DatePicker customEndDate;
    private int currentPeriodDays = 30;

    // Componentes do gráfico de pedidos
    private LineChart<Number, Number> ordersChart;
    private NumberAxis ordersXAxis;
    private NumberAxis ordersYAxis;

    // Componentes dos alertas de stock
    private GridPane stockAlertsGrid;
    private VBox criticalStockCard;
    private VBox lowStockCard;

    public DashboardView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
    }

    /**
     * Shows the dashboard with metrics cards, chart and scroll functionality
     */
    public void show() {
        // Clear previous content
        contentArea.getChildren().clear();

        // Create main container
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Create metrics grid
        createMetricsGrid();

        // Create stock alerts section
        createStockAlertsSection();

        // Create top items grid
        createTopItemsGrid();

        // Create sales chart
        createSalesChart();

        // Create last updated label
        createLastUpdatedLabel();

        // Add components to main container
        mainContainer.getChildren().addAll(
                createHeaderSection(),
                metricsGrid,
                createSectionHeader("Alertas de Stock"),
                stockAlertsGrid,
                createSectionHeader("Produtos Mais Vendidos"),
                itemsGrid,
                createSectionHeader("Vendas e Pedidos por Dia"),
                chartControlsContainer,
                salesChart,
                ordersChart,
                lastUpdatedLabel);

        // Create scroll pane for the entire dashboard
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("edge-to-edge");

        // Add scroll pane to content area
        contentArea.getChildren().add(scrollPane);

        // Load dashboard data
        loadDashboardMetrics();

        // Load stock alerts data
        loadStockAlerts();

        // Load top items data
        loadTopItems();

        // Load initial chart data
        loadChartsData(currentPeriodDays);
    }

    /**
     * Creates the header section with title and refresh button
     */
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Text titleText = new Text("Dashboard de Métricas");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleText.setFill(Color.valueOf("#333333"));

        // Subtitle
        Label subtitleLabel = new Label("Visão geral do desempenho do restaurante");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.valueOf("#666666"));

        // Refresh button
        Button refreshButton = new Button("Atualizar Dados");
        refreshButton.getStyleClass().add("login-button");
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.WHITE);
        refreshIcon.setIconSize(16);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> {
            loadDashboardMetrics();
            loadTopItems();
        });

        headerSection.getChildren().addAll(titleText, subtitleLabel, refreshButton);
        return headerSection;
    }

    /**
     * Creates the metrics grid with placeholder cards
     */
    private void createMetricsGrid() {
        metricsGrid = new GridPane();
        metricsGrid.setHgap(20);
        metricsGrid.setVgap(20);
        metricsGrid.setAlignment(Pos.CENTER);
        metricsGrid.setPadding(new Insets(20, 0, 20, 0));

        // Create loading cards
        createLoadingCards();
    }

    /**
     * Creates loading placeholder cards
     */
    private void createLoadingCards() {
        String[] cardTitles = {
                "Vendas Hoje", "Pedidos Hoje", "Ticket Médio", "Performance Hoje"
        };

        MaterialDesign[] icons = {
                MaterialDesign.MDI_CASH_MULTIPLE, MaterialDesign.MDI_RECEIPT,
                MaterialDesign.MDI_CALCULATOR, MaterialDesign.MDI_CHART_LINE
        };

        int row = 0, col = 0;
        for (int i = 0; i < cardTitles.length; i++) {
            VBox card = createMetricCard(cardTitles[i], "Carregando...", icons[i], Color.valueOf("#FB8C00"));
            metricsGrid.add(card, col, row);

            col++;
            if (col >= 2) { // Changed to 2 columns
                col = 0;
                row++;
            }
        }
    }

    /**
     * Creates a metric card with icon, title and value
     */
    private VBox createMetricCard(String title, String value, MaterialDesign icon, Color iconColor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(150);

        // Icon
        FontIcon cardIcon = new FontIcon(icon);
        cardIcon.setIconColor(iconColor);
        cardIcon.setIconSize(40);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.valueOf("#666666"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.valueOf("#333333"));
        valueLabel.setWrapText(true);
        valueLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(cardIcon, titleLabel, valueLabel);
        return card;
    }

    /**
     * Creates an enhanced metric card with trend indicators
     */
    private VBox createEnhancedMetricCard(String title, String value, String subtitle,
            double percentChange, String arrow, String color,
            MaterialDesign icon) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(320);
        card.setPrefHeight(180);
        card.setPadding(new Insets(20));

        // Header with icon and title
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon cardIcon = new FontIcon(icon);
        cardIcon.setIconColor(Color.valueOf("#2196F3"));
        cardIcon.setIconSize(24);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.valueOf("#666666"));

        header.getChildren().addAll(cardIcon, titleLabel);

        // Main value
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLabel.setTextFill(Color.valueOf("#1A1A1A"));

        // Trend indicator
        HBox trendBox = new HBox(5);
        trendBox.setAlignment(Pos.CENTER_LEFT);

        Label arrowLabel = new Label(arrow);
        arrowLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        Color trendColor = color.equals("green") ? Color.valueOf("#4CAF50")
                : color.equals("red") ? Color.valueOf("#F44336") : Color.valueOf("#FF9800");
        arrowLabel.setTextFill(trendColor);

        Label percentLabel = new Label(String.format("%.1f%%", Math.abs(percentChange)));
        percentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        percentLabel.setTextFill(trendColor);

        trendBox.getChildren().addAll(arrowLabel, percentLabel);

        // Subtitle
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        subtitleLabel.setTextFill(Color.valueOf("#999999"));

        card.getChildren().addAll(header, valueLabel, trendBox, subtitleLabel);
        return card;
    }

    /**
     * Creates a performance overview card
     */
    private VBox createPerformanceOverviewCard(double percentChange, String arrow, String color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(320);
        card.setPrefHeight(180);
        card.setPadding(new Insets(20));

        // Header
        Label titleLabel = new Label("Performance Hoje");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        // Large trend indicator
        VBox trendContainer = new VBox(5);
        trendContainer.setAlignment(Pos.CENTER);

        Label bigArrow = new Label(arrow);
        bigArrow.setFont(Font.font("System", FontWeight.BOLD, 48));
        Color trendColor = color.equals("green") ? Color.valueOf("#4CAF50")
                : color.equals("red") ? Color.valueOf("#F44336") : Color.valueOf("#FF9800");
        bigArrow.setTextFill(trendColor);

        Label bigPercent = new Label(String.format("%.1f%%", Math.abs(percentChange)));
        bigPercent.setFont(Font.font("System", FontWeight.BOLD, 24));
        bigPercent.setTextFill(trendColor);

        trendContainer.getChildren().addAll(bigArrow, bigPercent);

        // Status message
        String statusText = color.equals("green") ? "Excelente performance!"
                : color.equals("red") ? "Atenção necessária" : "Performance estável";
        Label statusLabel = new Label(statusText);
        statusLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        statusLabel.setTextFill(Color.valueOf("#666666"));

        card.getChildren().addAll(titleLabel, trendContainer, statusLabel);
        return card;
    }

    /**
     * Cria um cabeçalho de seção para separar áreas do dashboard
     */
    private HBox createSectionHeader(String title) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 5, 0));
        header.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 0, 0, 15));
        HBox.setHgrow(separator, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, separator);
        return header;
    }

    /**
     * Cria o grid para exibir os itens mais vendidos
     */
    private void createTopItemsGrid() {
        itemsGrid = new GridPane();
        itemsGrid.setHgap(20);
        itemsGrid.setVgap(20);
        itemsGrid.setAlignment(Pos.CENTER);
        itemsGrid.setPadding(new Insets(10, 0, 20, 0));

        // Criar placeholders para os itens
        for (int i = 0; i < TOP_ITEMS_COUNT; i++) {
            VBox placeholder = createTopItemPlaceholder(i + 1);
            itemsGrid.add(placeholder, i, 0);
        }
    }

    /**
     * Cria um placeholder para um item mais vendido
     */
    private VBox createTopItemPlaceholder(int position) {
        VBox card = new VBox(10);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(200);
        card.setPadding(new Insets(15));

        // Posição como medalha
        StackPane medalPane = createPositionMedal(position);

        Label titleLabel = new Label("Carregando...");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.valueOf("#555555"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(220);

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(40, 40);

        card.getChildren().addAll(medalPane, titleLabel, progress);
        return card;
    }

    /**
     * Cria uma medalha visual para indicar a posição do item no ranking
     */
    private StackPane createPositionMedal(int position) {
        StackPane medalPane = new StackPane();

        String color;
        switch (position) {
            case 1:
                color = "#FFD700"; // Gold
                break;
            case 2:
                color = "#C0C0C0"; // Silver
                break;
            case 3:
                color = "#CD7F32"; // Bronze
                break;
            default:
                color = "#1976D2"; // Blue
        }

        Circle medal = new Circle(25);
        medal.setFill(Color.valueOf(color));
        medal.setStroke(Color.valueOf("#FFFFFF"));
        medal.setStrokeWidth(2);
        medal.setEffect(new DropShadow(10, Color.valueOf("#00000044")));

        Text posText = new Text("#" + position);
        posText.setFont(Font.font("System", FontWeight.BOLD, 18));
        posText.setFill(Color.WHITE);

        medalPane.getChildren().addAll(medal, posText);
        return medalPane;
    }

    /**
     * Creates the last updated timestamp label
     */
    private void createLastUpdatedLabel() {
        lastUpdatedLabel = new Label();
        lastUpdatedLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lastUpdatedLabel.setTextFill(Color.valueOf("#999999"));
        lastUpdatedLabel.setAlignment(Pos.CENTER);
    }

    /**
     * Updates the last updated timestamp
     */
    private void updateLastUpdatedTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        lastUpdatedLabel.setText("Última atualização: " + timestamp);
    }

    /**
     * Loads dashboard metrics from the API
     */
    private void loadDashboardMetrics() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/dashboard/metrics")))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            try {
                                JsonNode metricsData = objectMapper.readTree(response.body());
                                updateMetricsCards(metricsData);
                                updateLastUpdatedTime();
                            } catch (Exception e) {
                                showErrorMessage("Erro ao processar dados de métricas: " + e.getMessage());
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            showErrorMessage("Erro ao carregar métricas. Status: " + response.statusCode());
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // Show demo data when API is not available
                        showDemoData();
                        updateLastUpdatedTime();
                    });
                    return null;
                });
    }

    /**
     * Updates the metrics cards with real data from API
     */
    private void updateMetricsCards(JsonNode data) {
        metricsGrid.getChildren().clear();

        // Extract data from the specific API response format
        double vendasDia = data.has("vendasDia") ? data.get("vendasDia").asDouble() : 0.0;
        int numeroPedidos = data.has("numeroPedidos") ? data.get("numeroPedidos").asInt() : 0;
        double ticketMedio = data.has("ticketMedio") ? data.get("ticketMedio").asDouble() : 0.0;
        double percentualMudanca = data.has("percentualMudanca") ? data.get("percentualMudanca").asDouble() : 0.0;
        String setinha = data.has("setinha") ? data.get("setinha").asText() : "→";
        String cor = data.has("cor") ? data.get("cor").asText() : "gray";
        double vendasOntem = data.has("vendasOntem") ? data.get("vendasOntem").asDouble() : 0.0;
        int pedidosOntem = data.has("pedidosOntem") ? data.get("pedidosOntem").asInt() : 0;

        // Create enhanced cards with trend indicators
        VBox vendasHojeCard = createEnhancedMetricCard("Vendas Hoje",
                currencyFormat.format(vendasDia),
                "Ontem: " + currencyFormat.format(vendasOntem),
                percentualMudanca, setinha, cor,
                MaterialDesign.MDI_CASH_MULTIPLE);

        VBox pedidosHojeCard = createEnhancedMetricCard("Pedidos Hoje",
                String.valueOf(numeroPedidos),
                "Ontem: " + pedidosOntem,
                percentualMudanca, setinha, cor,
                MaterialDesign.MDI_RECEIPT);

        VBox ticketMedioCard = createEnhancedMetricCard("Ticket Médio",
                currencyFormat.format(ticketMedio),
                "Valor médio por pedido",
                percentualMudanca, setinha, cor,
                MaterialDesign.MDI_CALCULATOR);

        // Performance overview card
        VBox performanceCard = createPerformanceOverviewCard(percentualMudanca, setinha, cor);

        // Add cards to grid in a 2x2 layout for better visual impact
        metricsGrid.add(vendasHojeCard, 0, 0);
        metricsGrid.add(pedidosHojeCard, 1, 0);
        metricsGrid.add(ticketMedioCard, 0, 1);
        metricsGrid.add(performanceCard, 1, 1);
    }

    /**
     * Shows demo data when API is not available
     */
    private void showDemoData() {
        metricsGrid.getChildren().clear();

        // Demo data based on the API format
        VBox vendasCard = createEnhancedMetricCard("Vendas Hoje", "€ 293,70",
                "Ontem: € 0,00", 100.0, "↑", "green", MaterialDesign.MDI_CASH_MULTIPLE);

        VBox pedidosCard = createEnhancedMetricCard("Pedidos Hoje", "7",
                "Ontem: 0", 100.0, "↑", "green", MaterialDesign.MDI_RECEIPT);

        VBox ticketCard = createEnhancedMetricCard("Ticket Médio", "€ 41,96",
                "Valor médio por pedido", 100.0, "↑", "green", MaterialDesign.MDI_CALCULATOR);

        VBox performanceCard = createPerformanceOverviewCard(100.0, "↑", "green");

        // Add cards to grid in 2x2 layout
        metricsGrid.add(vendasCard, 0, 0);
        metricsGrid.add(pedidosCard, 1, 0);
        metricsGrid.add(ticketCard, 0, 1);
        metricsGrid.add(performanceCard, 1, 1);

        // Show demo items
        itemsGrid.getChildren().clear();

        // Demo data para os itens mais vendidos
        VBox item1 = createTopItemCard(createDemoItem(19, "Ovos mexidos com torrada rústica", 5.00, 2), 26, 130.0, 1);
        VBox item2 = createTopItemCard(createDemoItem(8, "Café Expresso", 2.50, 4), 18, 36.0, 2);
        VBox item3 = createTopItemCard(createDemoItem(12, "Salada Caesar", 8.50, 1), 15, 75.0, 3);

        itemsGrid.add(item1, 0, 0);
        itemsGrid.add(item2, 1, 0);
        itemsGrid.add(item3, 2, 0);

        // Show demo notice
        lastUpdatedLabel.setText("Exibindo dados de demonstração - API não disponível");
    }

    /**
     * Cria um item de demonstração
     */
    private com.EatEaseFrontend.Item createDemoItem(int id, String nome, double preco, int tipoId) {
        com.EatEaseFrontend.Item item = new com.EatEaseFrontend.Item();
        // Definir manualmente os campos para demonstração
        try {
            java.lang.reflect.Field idField = item.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);

            java.lang.reflect.Field nomeField = item.getClass().getDeclaredField("nome");
            nomeField.setAccessible(true);
            nomeField.set(item, nome);

            java.lang.reflect.Field precoField = item.getClass().getDeclaredField("preco");
            precoField.setAccessible(true);
            precoField.set(item, preco);

            java.lang.reflect.Field tipoField = item.getClass().getDeclaredField("tipoPratoId");
            tipoField.setAccessible(true);
            tipoField.set(item, tipoId);
        } catch (Exception e) {
            System.err.println("Erro ao criar item de demo: " + e.getMessage());
        }
        return item;
    }

    /**
     * Shows error message in the dashboard
     */
    private void showErrorMessage(String message) {
        metricsGrid.getChildren().clear();

        VBox errorBox = new VBox(20);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.getStyleClass().add("dashboard-card");
        errorBox.setPrefWidth(600);
        errorBox.setPrefHeight(200);

        FontIcon errorIcon = new FontIcon(MaterialDesign.MDI_ALERT_CIRCLE);
        errorIcon.setIconColor(Color.valueOf("#F44336"));
        errorIcon.setIconSize(48);

        Label errorLabel = new Label("Erro ao Carregar Dashboard");
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        errorLabel.setTextFill(Color.valueOf("#F44336"));

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.valueOf("#666666"));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        Button retryButton = new Button("Tentar Novamente");
        retryButton.getStyleClass().add("login-button");
        retryButton.setOnAction(e -> {
            loadDashboardMetrics();
            loadTopItems();
        });

        errorBox.getChildren().addAll(errorIcon, errorLabel, messageLabel, retryButton);
        metricsGrid.add(errorBox, 0, 0, 2, 2); // Span 2 columns and 2 rows
        GridPane.setHalignment(errorBox, HPos.CENTER);
    }

    /**
     * Carrega os itens mais vendidos da API
     */
    private void loadTopItems() {
        // Limpar itens anteriores
        for (int position = 0; position < TOP_ITEMS_COUNT; position++) {
            final int pos = position;
            loadBestItemAtPosition(pos);
        }
    }

    /**
     * Carrega um item específico por posição no ranking
     */
    private void loadBestItemAtPosition(int position) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig
                        .getApiEndpoint("/dashboard/bestItem?lastDays=" + DEFAULT_DAYS + "&position=" + position)))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        // Processar o item e então carregar seu lucro
                        try {
                            com.EatEaseFrontend.Item item = objectMapper.readValue(response.body(),
                                    com.EatEaseFrontend.Item.class);
                            loadItemProfit(item, position);
                        } catch (Exception e) {
                            System.err.println("Erro ao processar item mais vendido: " + e.getMessage());
                            displayItemError(position);
                        }
                    } else {
                        displayItemError(position);
                    }
                })
                .exceptionally(ex -> {
                    displayItemError(position);
                    return null;
                });
    }

    /**
     * Carrega o lucro para um item específico
     */
    private void loadItemProfit(com.EatEaseFrontend.Item item, int position) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint(
                        "/dashboard/lucroByItemId?itemId=" + item.getId() + "&lastDays=" + DEFAULT_DAYS)))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode profitData = objectMapper.readTree(response.body());
                            int quantity = profitData.has("quantidade") ? profitData.get("quantidade").asInt() : 0;
                            double profit = profitData.has("lucro") ? profitData.get("lucro").asDouble() : 0.0;

                            Platform.runLater(() -> {
                                updateTopItemCard(item, quantity, profit, position);
                            });
                        } catch (Exception e) {
                            System.err.println("Erro ao processar lucro do item: " + e.getMessage());
                            displayItemError(position);
                        }
                    } else {
                        displayItemError(position);
                    }
                })
                .exceptionally(ex -> {
                    displayItemError(position);
                    return null;
                });
    }

    /**
     * Atualiza o card com dados do item mais vendido e seu lucro
     */
    private void updateTopItemCard(com.EatEaseFrontend.Item item, int quantity, double profit, int position) {
        VBox card = createTopItemCard(item, quantity, profit, position + 1);

        // Substitui o placeholder pelo card real
        if (position < TOP_ITEMS_COUNT) {
            itemsGrid.getChildren().remove(position);
            itemsGrid.add(card, position, 0);
        }
    }

    /**
     * Cria um card para exibir informações detalhadas de um item mais vendido
     */
    private VBox createTopItemCard(com.EatEaseFrontend.Item item, int quantity, double profit, int position) {
        VBox card = new VBox(12);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(250);
        card.setPrefHeight(260);
        card.setPadding(new Insets(15));

        // Posição como medalha
        StackPane medalPane = createPositionMedal(position);

        // Nome do item
        Label nameLabel = new Label(item.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.valueOf("#333333"));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(220);

        // Preço do item
        Label priceLabel = new Label(currencyFormat.format(item.getPreco()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.valueOf("#4CAF50"));

        // Tipo de prato
        Label typeLabel = new Label(item.getTipoPratoName());
        typeLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        typeLabel.setTextFill(Color.valueOf("#666666"));

        // Separador
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        // Estatísticas
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(5);

        // Quantidade vendida
        Label qtdTitle = new Label("Qtde. Vendida:");
        qtdTitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        qtdTitle.setTextFill(Color.valueOf("#666666"));

        Label qtdValue = new Label(String.valueOf(quantity));
        qtdValue.setFont(Font.font("System", FontWeight.BOLD, 14));
        qtdValue.setTextFill(Color.valueOf("#333333"));

        // Lucro gerado
        Label profitTitle = new Label("Lucro Gerado:");
        profitTitle.setFont(Font.font("System", FontWeight.NORMAL, 12));
        profitTitle.setTextFill(Color.valueOf("#666666"));

        Label profitValue = new Label(currencyFormat.format(profit));
        profitValue.setFont(Font.font("System", FontWeight.BOLD, 14));
        profitValue.setTextFill(Color.valueOf("#4CAF50"));

        double unitProfit = quantity > 0 ? profit / quantity : 0;
        Label unitProfitValue = new Label(currencyFormat.format(unitProfit));
        unitProfitValue.setFont(Font.font("System", FontWeight.BOLD, 14));
        unitProfitValue.setTextFill(Color.valueOf("#1976D2"));

        // Adicionar à grid
        statsGrid.add(qtdTitle, 0, 0);
        statsGrid.add(qtdValue, 1, 0);
        statsGrid.add(profitTitle, 0, 1);
        statsGrid.add(profitValue, 1, 1);
        statsGrid.add(unitProfitValue, 1, 2);

        // Montar o card
        card.getChildren().addAll(medalPane, nameLabel, priceLabel, typeLabel, separator, statsGrid);
        return card;
    }

    /**
     * Exibe um erro no card de item
     */
    private void displayItemError(int position) {
        Platform.runLater(() -> {
            if (position < TOP_ITEMS_COUNT) {
                VBox card = new VBox(15);
                card.getStyleClass().add("dashboard-card");
                card.setAlignment(Pos.CENTER);
                card.setPrefWidth(250);
                card.setPrefHeight(200);

                FontIcon errorIcon = new FontIcon(MaterialDesign.MDI_ALERT_CIRCLE);
                errorIcon.setIconColor(Color.valueOf("#F44336"));
                errorIcon.setIconSize(32);

                Label errorLabel = new Label("Erro ao carregar");
                errorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                errorLabel.setTextFill(Color.valueOf("#F44336"));

                Button retryButton = new Button("Tentar novamente");
                retryButton.getStyleClass().add("login-button");
                retryButton.setOnAction(e -> loadBestItemAtPosition(position));

                card.getChildren().addAll(errorIcon, errorLabel, retryButton);

                // Substitui o placeholder pelo card de erro
                itemsGrid.getChildren().remove(position);
                itemsGrid.add(card, position, 0);
            }
        });
    }

    /**
     * Creates the sales chart with time period controls
     */
    private void createSalesChart() {
        // Create sales chart axes
        salesXAxis = new NumberAxis();
        salesXAxis.setLabel("Dia do Mês");
        salesXAxis.setAutoRanging(false);
        salesXAxis.setLowerBound(1);
        salesXAxis.setUpperBound(31);
        salesXAxis.setTickUnit(5);

        salesYAxis = new NumberAxis();
        salesYAxis.setLabel("Vendas (€)");
        salesYAxis.setAutoRanging(true);

        // Create sales line chart
        salesChart = new LineChart<>(salesXAxis, salesYAxis);
        salesChart.setTitle("Vendas Diárias");
        salesChart.setCreateSymbols(true);
        salesChart.setLegendVisible(false);
        salesChart.setPrefHeight(400);
        salesChart.getStyleClass().add("dashboard-card");

        // Create orders chart axes
        ordersXAxis = new NumberAxis();
        ordersXAxis.setLabel("Dia do Mês");
        ordersXAxis.setAutoRanging(false);
        ordersXAxis.setLowerBound(1);
        ordersXAxis.setUpperBound(31);
        ordersXAxis.setTickUnit(5);

        ordersYAxis = new NumberAxis();
        ordersYAxis.setLabel("Número de Pedidos");
        ordersYAxis.setAutoRanging(true);

        // Create orders line chart
        ordersChart = new LineChart<>(ordersXAxis, ordersYAxis);
        ordersChart.setTitle("Pedidos Diários");
        ordersChart.setCreateSymbols(true);
        ordersChart.setLegendVisible(false);
        ordersChart.setPrefHeight(400);
        ordersChart.getStyleClass().add("dashboard-card");

        // Create time period controls
        createChartControls();
    }

    /**
     * Creates the time period selection controls for the chart
     */
    private void createChartControls() {
        chartControlsContainer = new HBox(10);
        chartControlsContainer.setAlignment(Pos.CENTER);
        chartControlsContainer.setPadding(new Insets(15));

        Label periodLabel = new Label("Período:");
        periodLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Toggle group for period selection
        periodToggleGroup = new ToggleGroup();

        // Period buttons
        ToggleButton btn7Days = new ToggleButton("7 dias");
        btn7Days.setToggleGroup(periodToggleGroup);
        btn7Days.getStyleClass().add("period-button");
        btn7Days.setOnAction(e -> {
            currentPeriodDays = 7;
            loadChartsData(7);
        });

        ToggleButton btn30Days = new ToggleButton("30 dias");
        btn30Days.setToggleGroup(periodToggleGroup);
        btn30Days.getStyleClass().add("period-button");
        btn30Days.setSelected(true); // Default selection
        btn30Days.setOnAction(e -> {
            currentPeriodDays = 30;
            loadChartsData(30);
        });

        ToggleButton btn90Days = new ToggleButton("90 dias");
        btn90Days.setToggleGroup(periodToggleGroup);
        btn90Days.getStyleClass().add("period-button");
        btn90Days.setOnAction(e -> {
            currentPeriodDays = 90;
            loadChartsData(90);
        });

        // Custom date range
        Label customLabel = new Label("Personalizado:");
        customLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));

        customStartDate = new DatePicker(LocalDate.now().minusDays(30));
        customStartDate.setPrefWidth(120);

        Label toLabel = new Label("até");

        customEndDate = new DatePicker(LocalDate.now());
        customEndDate.setPrefWidth(120);

        Button customApplyButton = new Button("Aplicar");
        customApplyButton.getStyleClass().add("login-button");
        customApplyButton.setOnAction(e -> loadCustomDateRange());

        // Refresh button
        Button refreshButton = new Button();
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.valueOf("#1976D2"));
        refreshIcon.setIconSize(16);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setOnAction(e -> loadChartsData(currentPeriodDays));

        chartControlsContainer.getChildren().addAll(
                periodLabel, btn7Days, btn30Days, btn90Days,
                new Separator(), customLabel, customStartDate, toLabel, customEndDate, customApplyButton,
                new Separator(), refreshButton);
    }

    /**
     * Loads both sales and orders chart data for the specified number of days
     */
    private void loadChartsData(int days) {
        loadSalesChartData(days);
        loadOrdersChartData(days);
    }

    /**
     * Loads sales chart data for the specified number of days
     */
    private void loadSalesChartData(int days) {
        // Clear existing data
        salesChart.getData().clear();

        // Create data series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Vendas Diárias");

        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Update x-axis bounds based on period
        if (days <= 31) {
            salesXAxis.setLowerBound(1);
            salesXAxis.setUpperBound(Math.max(days, 31));
            salesXAxis.setTickUnit(days <= 7 ? 1 : 5);
            salesXAxis.setLabel("Dia do Período");
        } else {
            salesXAxis.setLowerBound(1);
            salesXAxis.setUpperBound(days);
            salesXAxis.setTickUnit(days <= 30 ? 5 : 10);
            salesXAxis.setLabel("Dias atrás");
        }

        // Load data for each day
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            loadDailySalesData(currentDate, series, i + 1);
        }

        // Add series to chart
        salesChart.getData().add(series);
    }

    /**
     * Loads orders chart data for the specified number of days
     */
    private void loadOrdersChartData(int days) {
        // Clear existing data
        ordersChart.getData().clear();

        // Create data series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Pedidos Diários");

        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Update x-axis bounds based on period
        if (days <= 31) {
            ordersXAxis.setLowerBound(1);
            ordersXAxis.setUpperBound(Math.max(days, 31));
            ordersXAxis.setTickUnit(days <= 7 ? 1 : 5);
            ordersXAxis.setLabel("Dia do Período");
        } else {
            ordersXAxis.setLowerBound(1);
            ordersXAxis.setUpperBound(days);
            ordersXAxis.setTickUnit(days <= 30 ? 5 : 10);
            ordersXAxis.setLabel("Dias atrás");
        }

        // Load data for each day
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            loadDailyOrdersData(currentDate, series, i + 1);
        }

        // Add series to chart
        ordersChart.getData().add(series);
    }

    /**
     * Loads sales data for a specific date
     */
    private void loadDailySalesData(LocalDate date, XYChart.Series<Number, Number> series, int dayNumber) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endpoint = AppConfig.getApiEndpoint("/dashboard/vendas-dia?data=" + dateStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String responseBody = response.body().trim();
                            double salesValue = 0.0;

                            // Try to parse as direct number first
                            try {
                                salesValue = Double.parseDouble(responseBody);
                            } catch (NumberFormatException e1) {
                                // If that fails, try to parse as JSON
                                try {
                                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                                    salesValue = jsonNode.path("vendas").asDouble(0.0);
                                } catch (Exception e2) {
                                    System.err.println("Erro ao processar resposta: " + responseBody);
                                    salesValue = 0.0;
                                }
                            }

                            final double finalSalesValue = salesValue;
                            Platform.runLater(() -> {
                                series.getData().add(new XYChart.Data<>(dayNumber, finalSalesValue));
                            });
                        } catch (Exception e) {
                            System.err.println(
                                    "Erro ao processar dados de vendas para " + dateStr + ": " + e.getMessage());
                            Platform.runLater(() -> {
                                series.getData().add(new XYChart.Data<>(dayNumber, 0.0));
                            });
                        }
                    } else {
                        System.err.println("Erro na API de vendas diárias: " + response.statusCode());
                        Platform.runLater(() -> {
                            series.getData().add(new XYChart.Data<>(dayNumber, 0.0));
                        });
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Erro na requisição de vendas para " + dateStr + ": " + throwable.getMessage());
                    Platform.runLater(() -> {
                        series.getData().add(new XYChart.Data<>(dayNumber, 0.0));
                    });
                    return null;
                });
    }

    /**
     * Loads orders data for a specific date
     */
    private void loadDailyOrdersData(LocalDate date, XYChart.Series<Number, Number> series, int dayNumber) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endpoint = AppConfig.getApiEndpoint("/dashboard/pedidos-dia?data=" + dateStr);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            // Parse response as plain number (integer)
                            String responseBody = response.body().trim();
                            int ordersValue = Integer.parseInt(responseBody);

                            Platform.runLater(() -> {
                                series.getData().add(new XYChart.Data<>(dayNumber, ordersValue));
                            });
                        } catch (Exception e) {
                            System.err.println(
                                    "Erro ao processar dados de pedidos para " + dateStr + ": " + e.getMessage());
                            Platform.runLater(() -> {
                                series.getData().add(new XYChart.Data<>(dayNumber, 0));
                            });
                        }
                    } else {
                        System.err.println("Erro na API de pedidos diários: " + response.statusCode());
                        Platform.runLater(() -> {
                            series.getData().add(new XYChart.Data<>(dayNumber, 0));
                        });
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Erro na requisição de pedidos para " + dateStr + ": " + throwable.getMessage());
                    Platform.runLater(() -> {
                        series.getData().add(new XYChart.Data<>(dayNumber, 0));
                    });
                    return null;
                });
    }

    /**
     * Loads sales data for custom date range
     */
    private void loadCustomDateRange() {
        if (customStartDate.getValue() != null && customEndDate.getValue() != null) {
            LocalDate start = customStartDate.getValue();
            LocalDate end = customEndDate.getValue();

            if (start.isAfter(end)) {
                showAlert("Erro", "A data inicial deve ser anterior à data final.");
                return;
            }

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            if (daysBetween > 365) {
                showAlert("Erro", "O período selecionado não pode exceder 365 dias.");
                return;
            }

            // Clear selection from toggle buttons
            periodToggleGroup.selectToggle(null);

            // Load custom range data
            loadCustomRangeData(start, end);
        } else {
            showAlert("Erro", "Por favor, selecione as datas de início e fim.");
        }
    }

    /**
     * Loads sales and orders data for custom date range
     */
    private void loadCustomRangeData(LocalDate startDate, LocalDate endDate) {
        // Clear existing data for both charts
        salesChart.getData().clear();
        ordersChart.getData().clear();

        // Create data series for sales
        XYChart.Series<Number, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Vendas Diárias");

        // Create data series for orders
        XYChart.Series<Number, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Pedidos Diários");

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Update x-axis for both charts
        salesXAxis.setLowerBound(1);
        salesXAxis.setUpperBound(daysBetween);
        salesXAxis.setTickUnit(daysBetween <= 7 ? 1 : (daysBetween <= 30 ? 5 : 10));
        salesXAxis.setLabel("Dia do Período");

        ordersXAxis.setLowerBound(1);
        ordersXAxis.setUpperBound(daysBetween);
        ordersXAxis.setTickUnit(daysBetween <= 7 ? 1 : (daysBetween <= 30 ? 5 : 10));
        ordersXAxis.setLabel("Dia do Período");

        // Load data for each day in range
        for (int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            loadDailySalesData(currentDate, salesSeries, i + 1);
            loadDailyOrdersData(currentDate, ordersSeries, i + 1);
        }

        // Add series to charts
        salesChart.getData().add(salesSeries);
        ordersChart.getData().add(ordersSeries);
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Dispose method to clean up resources when switching views
     */
    public void dispose() {
        // Clean up any background tasks or resources if needed
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
    }

    /**
     * Creates the stock alerts section showing critical and low stock items
     */
    private void createStockAlertsSection() {
        // Create the main grid container for stock alerts
        stockAlertsGrid = new GridPane();
        stockAlertsGrid.setHgap(20);
        stockAlertsGrid.setVgap(20);
        stockAlertsGrid.setAlignment(Pos.CENTER);
        stockAlertsGrid.setPadding(new Insets(10, 0, 20, 0));

        // Create placeholder cards for critical and low stock
        criticalStockCard = createStockAlertCard("Crítico", Color.RED);
        lowStockCard = createStockAlertCard("Baixo", Color.ORANGE);

        // Add cards to the grid
        stockAlertsGrid.add(criticalStockCard, 0, 0);
        stockAlertsGrid.add(lowStockCard, 1, 0);
    }

    /**
     * Creates a card to display stock alerts for a specific level
     *
     * @param level Alert level label (Critical/Low)
     * @param color Alert color indicator
     * @return VBox containing the stock alert card
     */
    private VBox createStockAlertCard(String level, Color color) {
        VBox card = new VBox(15);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(400);
        card.setPrefHeight(300);
        card.setPadding(new Insets(20));

        // Header with icon and title
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Stock indicator circle
        Circle stockIndicator = new Circle(10);
        stockIndicator.setFill(color);
        stockIndicator.setStroke(Color.valueOf("#FFFFFF"));
        stockIndicator.setStrokeWidth(1);
        stockIndicator.setEffect(new DropShadow(5, Color.valueOf("#00000044")));

        // Card title
        Label titleLabel = new Label("Stock " + level);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));

        header.getChildren().addAll(stockIndicator, titleLabel);

        // Stock count indicator
        Label countLabel = new Label("Carregando...");
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        countLabel.setTextFill(color);

        // Description text
        String description = level.equals("Crítico") ? "Itens com stock ≤ 5 unidades"
                : "Itens com stock entre 6 e 10 unidades";

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.valueOf("#666666"));

        // Scroll pane for the list of items
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(160);

        // Container for item list
        VBox itemsList = new VBox(5);
        itemsList.setPadding(new Insets(5, 0, 5, 0));
        scrollPane.setContent(itemsList);

        // Initial loading indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(30, 30);

        itemsList.getChildren().add(progress);

        // Store the item list VBox as a property in the card for later updating
        card.getProperties().put("itemsList", itemsList);
        card.getProperties().put("countLabel", countLabel);

        // Add all components to card
        card.getChildren().addAll(header, countLabel, descLabel, scrollPane);

        return card;
    }

    /**
     * Loads and displays items with critical and low stock levels
     */
    private void loadStockAlerts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/item/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            // Parse items from response
                            JsonNode itemsNode = objectMapper.readTree(response.body());
                            if (itemsNode.isArray()) {
                                // Process items and update UI
                                Platform.runLater(() -> processStockItems(itemsNode));
                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> showStockAlertError("Erro ao processar dados: " + e.getMessage()));
                        }
                    } else {
                        Platform.runLater(() -> showStockAlertError("Erro na API: " + response.statusCode()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showStockAlertError("Erro de conexão: " + ex.getMessage()));
                    return null;
                });
    }

    /**
     * Process items from API and update the stock alert cards
     *
     * @param itemsNode JsonNode array of items
     */
    private void processStockItems(JsonNode itemsNode) {
        // Lists to store items with critical and low stock
        List<com.EatEaseFrontend.Item> criticalItems = new ArrayList<>();
        List<com.EatEaseFrontend.Item> lowStockItems = new ArrayList<>();

        // Parse each item and categorize it
        try {
            for (JsonNode node : itemsNode) {
                com.EatEaseFrontend.Item item = objectMapper.treeToValue(node, com.EatEaseFrontend.Item.class);

                // Check stock level
                if (item.getStockAtual() <= 5) {
                    criticalItems.add(item);
                } else if (item.getStockAtual() <= 10) {
                    lowStockItems.add(item);
                }
            }

            // Update UI with the results
            updateStockAlertCard(criticalStockCard, criticalItems);
            updateStockAlertCard(lowStockCard, lowStockItems);

        } catch (Exception e) {
            showStockAlertError("Erro ao processar itens: " + e.getMessage());
        }
    }

    /**
     * Updates a stock alert card with the list of items
     *
     * @param card  The card to update
     * @param items List of items to display
     */
    private void updateStockAlertCard(VBox card, List<com.EatEaseFrontend.Item> items) {
        // Get references to the components we need to update
        VBox itemsList = (VBox) card.getProperties().get("itemsList");
        Label countLabel = (Label) card.getProperties().get("countLabel");

        // Clear previous content
        itemsList.getChildren().clear();

        // Update count label
        countLabel.setText(String.valueOf(items.size()));

        if (items.isEmpty()) {
            // Show "No items" message if list is empty
            Label emptyLabel = new Label("Nenhum item neste nível de stock");
            emptyLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            emptyLabel.setTextFill(Color.valueOf("#999999"));
            itemsList.getChildren().add(emptyLabel);
        } else {
            // Add each item to the list
            for (com.EatEaseFrontend.Item item : items) {
                HBox itemRow = createStockItemRow(item);
                itemsList.getChildren().add(itemRow);

                // Add separator between items
                if (items.indexOf(item) < items.size() - 1) {
                    Separator separator = new Separator();
                    separator.setPadding(new Insets(2, 0, 2, 0));
                    itemsList.getChildren().add(separator);
                }
            }
        }
    }

    /**
     * Creates a row displaying an item's stock information
     *
     * @param item The item to display
     * @return HBox containing the item info row
     */
    private HBox createStockItemRow(com.EatEaseFrontend.Item item) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));

        // Stock indicator circle
        Circle stockCircle = new Circle(6);
        Color indicatorColor = item.getStockAtual() <= 5 ? Color.RED : Color.ORANGE;
        stockCircle.setFill(indicatorColor);

        // Item name
        Label nameLabel = new Label(item.getNome());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.valueOf("#333333"));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Stock quantity
        Label stockLabel = new Label(item.getStockAtual() + " un.");
        stockLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        stockLabel.setTextFill(indicatorColor);

        row.getChildren().addAll(stockCircle, nameLabel, stockLabel);
        return row;
    }

    /**
     * Shows error message in the stock alerts section
     */
    private void showStockAlertError(String errorMessage) {
        // Clear both cards
        VBox criticalItems = (VBox) criticalStockCard.getProperties().get("itemsList");
        VBox lowStockItems = (VBox) lowStockCard.getProperties().get("itemsList");

        criticalItems.getChildren().clear();
        lowStockItems.getChildren().clear();

        // Add error message to both cards
        Label errorLabel = new Label("Erro: " + errorMessage);
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);

        Button retryButton = new Button("Tentar Novamente");
        retryButton.getStyleClass().add("login-button");
        retryButton.setOnAction(e -> loadStockAlerts());

        VBox errorBox = new VBox(10, errorLabel, retryButton);
        errorBox.setAlignment(Pos.CENTER);

        criticalItems.getChildren().add(errorBox);

        // Update count labels
        Label criticalCountLabel = (Label) criticalStockCard.getProperties().get("countLabel");
        Label lowStockCountLabel = (Label) lowStockCard.getProperties().get("countLabel");

        criticalCountLabel.setText("-");
        lowStockCountLabel.setText("-");
    }
}
