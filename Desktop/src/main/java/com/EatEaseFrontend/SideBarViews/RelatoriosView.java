package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.DialogHelper;
import com.EatEaseFrontend.Item;
import com.EatEaseFrontend.ItemJsonLoader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * View for generating and exporting restaurant reports to PDF
 */
public class RelatoriosView {
    private final StackPane contentArea;
    private final HttpClient httpClient;
    private VBox mainContainer;
    private GridPane reportsGrid;
    private Label lastUpdatedLabel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    // Data containers
    private Map<String, JsonNode> dailySalesData;
    private Map<String, JsonNode> dailyOrdersData;
    private Map<Integer, Map<String, JsonNode>> itemProfitData; // itemId -> date -> profit data
    private List<Item> allItems;

    // Date range controls
    private DatePicker startDate;
    private DatePicker endDate;

    public RelatoriosView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
        this.dailySalesData = new HashMap<>();
        this.dailyOrdersData = new HashMap<>();
        this.itemProfitData = new HashMap<>();
        this.allItems = new ArrayList<>();
    }

    /**
     * Shows the reports view with report options and PDF export functionality
     */
    public void show() {
        // Clear previous content
        contentArea.getChildren().clear();

        // Create main container
        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Create header
        VBox headerSection = createHeaderSection();

        // Create date range selector
        VBox dateRangeSelector = createDateRangeSelector();

        // Create reports grid
        createReportsGrid();

        // Create last updated label
        createLastUpdatedLabel();

        // Create PDF export button
        Button exportPdfButton = createPdfExportButton();

        // Create a container for the export button
        HBox exportButtonContainer = new HBox(exportPdfButton);
        exportButtonContainer.setAlignment(Pos.CENTER);
        exportButtonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Add components to main container
        mainContainer.getChildren().addAll(
                headerSection,
                dateRangeSelector,
                createSectionHeader("Relatórios Disponíveis"),
                reportsGrid,
                exportButtonContainer,
                lastUpdatedLabel);

        // Create scroll pane for the entire view
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("edge-to-edge");

        // Add scroll pane to content area
        contentArea.getChildren().add(scrollPane);

        // Load initial data
        loadInitialData();
    }

    /**
     * Creates the header section with title and refresh button
     */
    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Text titleText = new Text("Relatórios");
        titleText.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleText.setFill(Color.valueOf("#333333"));

        // Subtitle
        Label subtitleLabel = new Label("Gere e exporte relatórios do seu restaurante");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.valueOf("#666666"));

        // Refresh button
        Button refreshButton = new Button("Atualizar Dados");
        refreshButton.getStyleClass().add("login-button");
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.WHITE);
        refreshIcon.setIconSize(16);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setOnAction(e -> loadInitialData());

        headerSection.getChildren().addAll(titleText, subtitleLabel, refreshButton);
        return headerSection;
    }

    /**
     * Creates a date range selector for report generation
     */
    private VBox createDateRangeSelector() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("dashboard-card");
        container.setPadding(new Insets(20));

        Label titleLabel = new Label("Selecione o Período do Relatório");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox dateControls = new HBox(20);
        dateControls.setAlignment(Pos.CENTER);

        // Start date
        VBox startDateBox = new VBox(5);
        startDateBox.setAlignment(Pos.CENTER_LEFT);
        Label startDateLabel = new Label("Data Inicial:");
        startDate = new DatePicker(LocalDate.now().minusDays(30));

        // Add listener to reload data when start date changes
        startDate.setOnAction(e -> {
            // Validate that start date is not after end date
            if (startDate.getValue() != null && endDate.getValue() != null &&
                    startDate.getValue().isAfter(endDate.getValue())) {
                endDate.setValue(startDate.getValue());
            }
            // Reload data for the new date range
            loadInitialData();
        });

        startDateBox.getChildren().addAll(startDateLabel, startDate);

        // End date (now editable)
        VBox endDateBox = new VBox(5);
        endDateBox.setAlignment(Pos.CENTER_LEFT);
        Label endDateLabel = new Label("Data Final:");
        endDate = new DatePicker(LocalDate.now());

        // Add listener to reload data when end date changes
        endDate.setOnAction(e -> {
            // Validate that end date is not before start date
            if (startDate.getValue() != null && endDate.getValue() != null &&
                    endDate.getValue().isBefore(startDate.getValue())) {
                startDate.setValue(endDate.getValue());
            }
            // Reload data for the new date range
            loadInitialData();
        });

        endDateBox.getChildren().addAll(endDateLabel, endDate);

        dateControls.getChildren().addAll(startDateBox, endDateBox);
        container.getChildren().addAll(titleLabel, dateControls);

        return container;
    }

    /**
     * Creates the reports grid with available report types
     */
    private void createReportsGrid() {
        reportsGrid = new GridPane();
        reportsGrid.setHgap(20);
        reportsGrid.setVgap(20);
        reportsGrid.setAlignment(Pos.CENTER);
        reportsGrid.setPadding(new Insets(20, 0, 20, 0));

        // Create report option cards
        String[] reportTitles = {
                "Relatório de Vendas",
                "Relatório de Produtos", "Relatório de Stock"
        };

        String[] reportDescriptions = {
                "Análise detalhada das vendas e pedidos por período",
                "Relatório dos produtos mais vendidos",
                "Alertas de stock e gestão de inventário"
        };

        MaterialDesign[] icons = {
                MaterialDesign.MDI_CASH_MULTIPLE,
                MaterialDesign.MDI_FOOD, MaterialDesign.MDI_PACKAGE_VARIANT
        };

        int row = 0, col = 0;
        for (int i = 0; i < reportTitles.length; i++) {
            VBox reportCard = createReportCard(reportTitles[i], reportDescriptions[i], icons[i]);
            reportsGrid.add(reportCard, col, row);
            GridPane.setHalignment(reportCard, HPos.CENTER);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Creates a report type option card
     */
    private VBox createReportCard(String title, String description, MaterialDesign icon) {
        VBox card = new VBox(15);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(300);
        card.setPrefHeight(200);

        // Icon
        FontIcon cardIcon = new FontIcon(icon);
        cardIcon.setIconColor(Color.valueOf("#0078d7"));
        cardIcon.setIconSize(40);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.valueOf("#333333"));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.valueOf("#666666"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        // Select checkbox
        CheckBox selectCheckBox = new CheckBox("Incluir no PDF");
        selectCheckBox.setSelected(true);

        card.getChildren().addAll(cardIcon, titleLabel, descLabel, selectCheckBox);
        return card;
    }

    /**
     * Creates a section header with title
     */
    private HBox createSectionHeader(String title) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 5, 0));

        Label headerLabel = new Label(title);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.valueOf("#333333"));

        header.getChildren().add(headerLabel);
        return header;
    }

    /**
     * Creates the PDF export button
     */
    private Button createPdfExportButton() {
        Button exportButton = new Button("Exportar PDF");
        exportButton.getStyleClass().add("login-button");
        exportButton.setPrefWidth(200);
        exportButton.setPrefHeight(40);

        FontIcon pdfIcon = new FontIcon(MaterialDesign.MDI_FILE_PDF);
        pdfIcon.setIconColor(Color.WHITE);
        pdfIcon.setIconSize(18);
        exportButton.setGraphic(pdfIcon);

        exportButton.setOnAction(e -> exportToPdf());

        return exportButton;
    }

    /**
     * Creates the last updated timestamp label
     */
    private void createLastUpdatedLabel() {
        lastUpdatedLabel = new Label();
        lastUpdatedLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lastUpdatedLabel.setTextFill(Color.valueOf("#999999"));
        lastUpdatedLabel.setAlignment(Pos.CENTER);
        updateLastUpdatedTime();
    }

    /**
     * Updates the last updated timestamp
     */
    private void updateLastUpdatedTime() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Platform.runLater(() -> lastUpdatedLabel.setText("Dados atualizados em: " + timestamp));
    }

    /**
     * Loads initial data for reports
     */
    private void loadInitialData() {
        loadHistoricalSalesData();
        loadHistoricalOrdersData();
        loadAllItemsData();
        loadItemProfitData();
    }

    /**
     * Loads historical sales data for multiple days using the correct API
     */
    private void loadHistoricalSalesData() {
        dailySalesData.clear();
        LocalDate endDate = this.endDate.getValue();
        LocalDate startDate = this.startDate.getValue();

        // Load data for each day in the range
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            loadSalesDataForDate(date);
        }
    }

    /**
     * Loads sales data for a specific date
     */
    private void loadSalesDataForDate(LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/dashboard/vendas-dia?data=" + dateStr)))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            try {
                                String responseBody = resp.body().trim();
                                if (responseBody.startsWith("<")) {
                                    System.out.println("Sales API returned HTML error page for date: " + dateStr);
                                    return;
                                }

                                // Parse response similar to DashboardView pattern
                                JsonNode salesNode;
                                try {
                                    // Try to parse as direct number first
                                    double salesValue = Double.parseDouble(responseBody);
                                    // Create a JSON node with the expected structure
                                    salesNode = objectMapper.createObjectNode()
                                            .put("vendasDia", salesValue)
                                            .put("numeroPedidos", 0) // Default value
                                            .put("ticketMedio", 0.0); // Default value
                                } catch (NumberFormatException e1) {
                                    // If that fails, try to parse as JSON
                                    try {
                                        salesNode = objectMapper.readTree(responseBody);
                                    } catch (Exception e2) {
                                        System.err.println("Erro ao processar resposta de vendas: " + responseBody);
                                        return;
                                    }
                                }

                                dailySalesData.put(dateStr, salesNode);
                                Platform.runLater(() -> updateLastUpdatedTime());
                            } catch (Exception e) {
                                System.out.println(
                                        "Error parsing sales data for date " + dateStr + ": " + e.getMessage());
                                Platform.runLater(() -> {
                                    showErrorMessage("Erro ao processar dados de vendas para " + dateStr + ": "
                                            + e.getMessage());
                                });
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println(
                                    "Failed to load sales data for date " + dateStr + ". Status: " + resp.statusCode());
                            Platform.runLater(() -> {
                                showErrorMessage(
                                        "Erro ao carregar vendas para " + dateStr + ". Status: " + resp.statusCode());
                            });
                        }
                    })
                    .exceptionally(e -> {
                        System.out.println("Failed to load sales data for date: " + dateStr);
                        Platform.runLater(() -> {
                            showErrorMessage(
                                    "Erro ao carregar dados de vendas para " + dateStr + ": " + e.getMessage());
                        });
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads historical orders data for multiple days
     */
    private void loadHistoricalOrdersData() {
        dailyOrdersData.clear();
        LocalDate endDate = this.endDate.getValue();
        LocalDate startDate = this.startDate.getValue();

        // Load data for each day in the range
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            loadOrdersDataForDate(date);
        }
    }

    /**
     * Loads orders data for a specific date
     */
    private void loadOrdersDataForDate(LocalDate date) {
        try {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/dashboard/pedidos-dia?data=" + dateStr)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            try {
                                String responseBody = resp.body().trim();
                                if (responseBody.startsWith("<")) {
                                    System.out.println("Orders API returned HTML error page for date: " + dateStr);
                                    return;
                                }

                                JsonNode ordersNode;
                                try {
                                    // Try parsing as direct number first
                                    int ordersValue = Integer.parseInt(responseBody);
                                    ordersNode = objectMapper.createObjectNode().put("value", ordersValue);
                                } catch (NumberFormatException e) {
                                    // Fall back to JSON parsing
                                    ordersNode = objectMapper.readTree(responseBody);
                                }

                                dailyOrdersData.put(dateStr, ordersNode);
                                Platform.runLater(() -> updateLastUpdatedTime());
                            } catch (Exception e) {
                                System.out.println(
                                        "Error parsing orders data for date " + dateStr + ": " + e.getMessage());
                                Platform.runLater(() -> {
                                    showErrorMessage("Erro ao processar dados de pedidos para " + dateStr + ": "
                                            + e.getMessage());
                                });
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Failed to load orders data for date " + dateStr + ". Status: "
                                    + resp.statusCode());
                            Platform.runLater(() -> {
                                showErrorMessage(
                                        "Erro ao carregar pedidos para " + dateStr + ". Status: " + resp.statusCode());
                            });
                        }
                    })
                    .exceptionally(e -> {
                        System.out.println("Failed to load orders data for date: " + dateStr + ": " + e.getMessage());
                        Platform.runLater(() -> {
                            showErrorMessage(
                                    "Erro ao carregar dados de pedidos para " + dateStr + ": " + e.getMessage());
                        });
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all items data to get item IDs for profit analysis
     */
    private void loadAllItemsData() {
        try {
            // Make API request to get items
            HttpRequest getItemsReq = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/item/getAll")))
                    .GET()
                    .build();

            httpClient.sendAsync(getItemsReq, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            try {
                                String responseBody = resp.body();
                                if (responseBody.trim().startsWith("<")) {
                                    Platform.runLater(() -> {
                                        showErrorMessage(
                                                "Erro no servidor: resposta HTML recebida em vez de dados JSON");
                                    });
                                    return;
                                }

                                allItems.clear();
                                allItems.addAll(ItemJsonLoader.parseItems(responseBody));
                                // After loading items, load profit data for each item
                                loadItemProfitData();
                                Platform.runLater(() -> updateLastUpdatedTime());
                            } catch (Exception e) {
                                System.out.println("Error parsing items data: " + e.getMessage());
                                Platform.runLater(() -> {
                                    showErrorMessage("Erro ao processar dados dos produtos: " + e.getMessage());
                                });
                                e.printStackTrace();
                            }
                        } else {
                            Platform.runLater(() -> {
                                showErrorMessage("Erro ao carregar produtos. Status: " + resp.statusCode());
                            });
                        }
                    })
                    .exceptionally(e -> {
                        System.out.println("Failed to load items data: " + e.getMessage());
                        Platform.runLater(() -> {
                            showErrorMessage("Erro ao carregar dados dos produtos: " + e.getMessage());
                        });
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showErrorMessage("Erro ao carregar dados dos produtos: " + e.getMessage());
            });
            e.printStackTrace();
        }
    }

    /**
     * Loads profit data for all items over the last 10 days
     */
    private void loadItemProfitData() {
        for (Item item : allItems) {
            loadItemProfitForLastDays(item.getId(), 10);
        }
    }

    /**
     * Loads profit data for a specific item over the last N days
     */
    private void loadItemProfitForLastDays(int itemId, int lastDays) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint(
                            "/dashboard/lucroByItemId?itemId=" + itemId + "&lastDays=" + lastDays)))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> {
                        if (resp.statusCode() == 200) {
                            try {
                                String responseBody = resp.body();
                                if (responseBody.trim().startsWith("<")) {
                                    System.out.println("Profit API returned HTML error page for item: " + itemId);
                                    return;
                                }

                                JsonNode profitNode = objectMapper.readTree(responseBody);

                                // Store profit data by item ID
                                if (!itemProfitData.containsKey(itemId)) {
                                    itemProfitData.put(itemId, new HashMap<>());
                                }

                                // Process daily profit data
                                if (profitNode.isArray()) {
                                    for (JsonNode dayData : profitNode) {
                                        if (dayData.has("data")) {
                                            String dateStr = dayData.get("data").asText();
                                            itemProfitData.get(itemId).put(dateStr, dayData);
                                        }
                                    }
                                }

                                Platform.runLater(() -> updateLastUpdatedTime());
                            } catch (Exception e) {
                                System.out.println(
                                        "Error parsing profit data for item " + itemId + ": " + e.getMessage());
                                Platform.runLater(() -> {
                                    showErrorMessage("Erro ao processar dados de lucro para item " + itemId + ": "
                                            + e.getMessage());
                                });
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println(
                                    "Failed to load profit data for item " + itemId + ". Status: " + resp.statusCode());
                            Platform.runLater(() -> {
                                showErrorMessage("Erro ao carregar lucro para item " + itemId + ". Status: "
                                        + resp.statusCode());
                            });
                        }
                    })
                    .exceptionally(e -> {
                        System.out.println("Failed to load profit data for item " + itemId + ": " + e.getMessage());
                        Platform.runLater(() -> {
                            showErrorMessage(
                                    "Erro ao carregar dados de lucro para item " + itemId + ": " + e.getMessage());
                        });
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows error message in the reports view
     */
    private void showErrorMessage(String message) {
        DialogHelper.showErrorAlert("Erro", "Falha ao Carregar Dados", message);
    }

    /**
     * Export all the report data to a PDF file
     */
    private void exportToPdf() {
        // Get selected report types from the grid
        List<String> selectedReports = getSelectedReports();

        if (selectedReports.isEmpty()) {
            DialogHelper.showErrorAlert("Nenhum relatório selecionado",
                    "Por favor, selecione pelo menos um tipo de relatório para exportar.");
            return;
        }

        // Debug information
        System.out.println("Selected reports: " + selectedReports);
        System.out.println("Sales data size: " + dailySalesData.size());
        System.out.println("Orders data size: " + dailyOrdersData.size());
        System.out.println("Items size: " + allItems.size());

        // Show confirmation popup before proceeding with PDF generation
        String reportsList = String.join(", ", selectedReports);
        String confirmationMessage = String.format(
                "Deseja gerar o PDF com os seguintes relatórios?\n\n%s\n\nPeríodo: %s a %s",
                reportsList,
                startDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                endDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        PopUp.showConfirmationPopup(
                Alert.AlertType.INFORMATION,
                "Confirmar Geração de PDF",
                "Geração de Relatório",
                confirmationMessage,
                () -> {
                    // User confirmed, proceed with PDF generation
                    generatePdfFile(selectedReports);
                });
    }

    /**
     * Generates the actual PDF file after user confirmation
     */
    private void generatePdfFile(List<String> selectedReports) {
        // Choose the directory to save the PDF
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Salvar Relatório PDF");
        File directory = directoryChooser.showDialog(null);

        if (directory == null) {
            return; // User canceled the operation
        }

        // Generate the PDF filename with date
        String filename = String.format("Relatorio_EatEase_%s.pdf",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        File pdfFile = new File(directory, filename);

        try {
            // Create PDF document
            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Add content to the PDF
            createPdfContent(document, selectedReports);

            // Close the document
            document.close();

            // Show success message using popup
            Platform.runLater(() -> {
                PopUp.showPopupDialog(
                        Alert.AlertType.INFORMATION,
                        "PDF Criado com Sucesso",
                        "Relatório Gerado",
                        "O relatório foi salvo como:\n" + pdfFile.getAbsolutePath());
            });

        } catch (FileNotFoundException e) {
            PopUp.showPopupDialog(
                    Alert.AlertType.ERROR,
                    "Erro ao Salvar Arquivo",
                    "Falha na Criação do PDF",
                    "Não foi possível salvar o arquivo. O arquivo está aberto em outro programa?");
            e.printStackTrace();
        } catch (Exception e) {
            PopUp.showPopupDialog(
                    Alert.AlertType.ERROR,
                    "Erro ao Gerar PDF",
                    "Falha na Geração do Relatório",
                    "Ocorreu um erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets list of selected report types from the grid
     */
    private List<String> getSelectedReports() {
        List<String> selectedReports = new ArrayList<>();

        // Check each report card in the grid for selected checkboxes
        for (javafx.scene.Node node : reportsGrid.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                // Get the report title
                for (javafx.scene.Node child : card.getChildren()) {
                    if (child instanceof CheckBox) {
                        CheckBox checkbox = (CheckBox) child;
                        if (checkbox.isSelected()) {
                            // Find corresponding label
                            for (javafx.scene.Node titleNode : card.getChildren()) {
                                if (titleNode instanceof Label
                                        && ((Label) titleNode).getStyleClass().contains("card-title")) {
                                    selectedReports.add(((Label) titleNode).getText());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        return selectedReports;
    }

    /**
     * Create the content for the PDF document
     */
    private void createPdfContent(Document document, List<String> selectedReports) throws IOException {
        // Define colors
        DeviceRgb primaryColor = new DeviceRgb(0, 120, 215); // #0078D7
        DeviceRgb lightGray = new DeviceRgb(237, 237, 237); // #EDEDED

        // Load fonts
        PdfFont fontBold = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont fontRegular = PdfFontFactory.createFont("Helvetica");

        // Add document title
        Paragraph title = new Paragraph("EatEase Restaurant - Relatório Completo")
                .setFont(fontBold)
                .setFontSize(20)
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        // Add date range
        String periodText = String.format("Período do Relatório: %s a %s",
                startDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                endDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Paragraph datePeriod = new Paragraph(periodText)
                .setFont(fontRegular)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(datePeriod);

        // Add generation datetime
        String genDateTime = "Gerado em: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        Paragraph genDateParagraph = new Paragraph(genDateTime)
                .setFont(fontRegular)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(genDateParagraph);

        document.add(new Paragraph("\n")); // Space

        // Add selected report sections
        for (String reportType : selectedReports) {
            switch (reportType) {
                case "Relatório de Vendas":
                    addHistoricalSalesReport(document, fontBold, fontRegular, primaryColor, lightGray);
                    break;
                case "Relatório de Produtos":
                    addProductsProfitReport(document, fontBold, fontRegular, primaryColor, lightGray);
                    break;
                case "Relatório de Stock":
                    addStockAlertsReport(document, fontBold, fontRegular, primaryColor, lightGray);
                    break;
            }
        }
    }

    /**
     * Filters data by the selected date range
     */
    private boolean isDateInRange(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate start = startDate.getValue();
            LocalDate end = endDate.getValue();

            return !date.isBefore(start) && !date.isAfter(end);
        } catch (Exception e) {
            System.out.println("Error parsing date: " + dateStr);
            return false;
        }
    }

    /**
     * Gets filtered sales data within the selected date range
     */
    private Map<String, JsonNode> getFilteredSalesData() {
        Map<String, JsonNode> filteredData = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : dailySalesData.entrySet()) {
            if (isDateInRange(entry.getKey())) {
                filteredData.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredData;
    }

    /**
     * Gets filtered orders data within the selected date range
     */
    private Map<String, JsonNode> getFilteredOrdersData() {
        Map<String, JsonNode> filteredData = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : dailyOrdersData.entrySet()) {
            if (isDateInRange(entry.getKey())) {
                filteredData.put(entry.getKey(), entry.getValue());
            }
        }
        return filteredData;
    }

    /**
     * Add comprehensive sales report section to PDF
     */
    private void addHistoricalSalesReport(Document document, PdfFont fontBold, PdfFont fontRegular,
            DeviceRgb primaryColor, DeviceRgb lightGray) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Relatório de Vendas e Pedidos - Timeline Completa")
                .setFont(fontBold)
                .setFontSize(16)
                .setFontColor(primaryColor);
        document.add(sectionTitle);

        document.add(new Paragraph("\n")); // Space

        // Get filtered data for the selected date range
        Map<String, JsonNode> filteredSalesData = getFilteredSalesData();
        Map<String, JsonNode> filteredOrdersData = getFilteredOrdersData();

        if (!filteredSalesData.isEmpty()) {
            // Calculate totals
            double totalSales = 0.0;
            int totalOrders = 0;
            int daysWithData = 0;

            // Create detailed sales table
            Table salesTable = new Table(UnitValue.createPercentArray(new float[] { 2, 2, 2, 2 }))
                    .useAllAvailableWidth();

            // Header row
            salesTable.addHeaderCell(new Cell().add(new Paragraph("Data")
                    .setFont(fontBold).setFontColor(primaryColor)));
            salesTable.addHeaderCell(new Cell().add(new Paragraph("Vendas (€)")
                    .setFont(fontBold).setFontColor(primaryColor)));
            salesTable.addHeaderCell(new Cell().add(new Paragraph("Nº Pedidos")
                    .setFont(fontBold).setFontColor(primaryColor)));
            salesTable.addHeaderCell(new Cell().add(new Paragraph("Ticket Médio (€)")
                    .setFont(fontBold).setFontColor(primaryColor)));

            // Sort dates and add data
            List<String> sortedDates = new ArrayList<>(filteredSalesData.keySet());
            Collections.sort(sortedDates);

            for (String dateStr : sortedDates) {
                JsonNode salesData = filteredSalesData.get(dateStr);
                JsonNode ordersData = filteredOrdersData.get(dateStr);

                double dayVendas = salesData.has("vendasDia") ? salesData.get("vendasDia").asDouble() : 0.0;

                // Get orders count from orders data if available, otherwise from sales data
                int dayOrders = 0;
                if (ordersData != null) {
                    dayOrders = ordersData.has("value") ? ordersData.get("value").asInt()
                            : ordersData.has("totalPedidos") ? ordersData.get("totalPedidos").asInt() : 0;
                } else if (salesData.has("numeroPedidos")) {
                    dayOrders = salesData.get("numeroPedidos").asInt();
                }

                double dayTicket = dayOrders > 0 ? dayVendas / dayOrders : 0.0;

                // Format date for display
                try {
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String displayDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    salesTable.addCell(new Cell().add(new Paragraph(displayDate).setFont(fontRegular)));
                } catch (Exception e) {
                    salesTable.addCell(new Cell().add(new Paragraph(dateStr).setFont(fontRegular)));
                }

                salesTable
                        .addCell(new Cell().add(new Paragraph(currencyFormat.format(dayVendas)).setFont(fontRegular)));
                salesTable.addCell(new Cell().add(new Paragraph(String.valueOf(dayOrders)).setFont(fontRegular)));
                salesTable
                        .addCell(new Cell().add(new Paragraph(currencyFormat.format(dayTicket)).setFont(fontRegular)));

                // Update totals
                totalSales += dayVendas;
                totalOrders += dayOrders;
                daysWithData++;
            }

            document.add(salesTable);

            // Add summary statistics
            document.add(new Paragraph("\n"));

            // Create dynamic period description
            LocalDate startDateValue = startDate.getValue();
            LocalDate endDateValue = endDate.getValue();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDateValue, endDateValue) + 1;

            String periodDescription;
            if (daysBetween <= 7) {
                periodDescription = String.format("Resumo dos Últimos %d Dias", daysBetween);
            } else if (daysBetween <= 31) {
                periodDescription = String.format("Resumo do Último Mês (%d dias)", daysBetween);
            } else if (daysBetween <= 93) {
                long months = daysBetween / 30;
                periodDescription = String.format("Resumo dos Últimos %d Meses", months);
            } else {
                periodDescription = String.format("Resumo do Período (%s a %s)",
                        startDateValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        endDateValue.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            Paragraph summaryTitle = new Paragraph(periodDescription)
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(primaryColor);
            document.add(summaryTitle);

            Table summaryTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            summaryTable.addCell(new Cell().add(new Paragraph("Total de Vendas").setFont(fontBold)));
            summaryTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(totalSales)).setFont(fontRegular)));

            summaryTable.addCell(new Cell().add(new Paragraph("Total de Pedidos").setFont(fontBold)));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(totalOrders)).setFont(fontRegular)));

            if (daysWithData > 0) {
                double avgDailySales = totalSales / daysWithData;
                double avgDailyOrders = (double) totalOrders / daysWithData;
                double avgTicket = totalOrders > 0 ? totalSales / totalOrders : 0;

                summaryTable.addCell(new Cell().add(new Paragraph("Média Diária de Vendas").setFont(fontRegular)));
                summaryTable.addCell(
                        new Cell().add(new Paragraph(currencyFormat.format(avgDailySales)).setFont(fontRegular)));

                summaryTable.addCell(new Cell().add(new Paragraph("Média Diária de Pedidos").setFont(fontRegular)));
                summaryTable.addCell(
                        new Cell().add(new Paragraph(String.format("%.1f", avgDailyOrders)).setFont(fontRegular)));

                summaryTable.addCell(new Cell().add(new Paragraph("Ticket Médio Geral").setFont(fontBold)));
                summaryTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(avgTicket)).setFont(fontBold)));
            }

            document.add(summaryTable);
        } else {
            document.add(
                    new Paragraph("Dados de vendas não disponíveis para o período selecionado").setFont(fontRegular));
        }

        document.add(new Paragraph("\n\n")); // Space
    }

    /**
     * Add products profit analysis report section to PDF
     */
    private void addProductsProfitReport(Document document, PdfFont fontBold, PdfFont fontRegular,
            DeviceRgb primaryColor, DeviceRgb lightGray) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Relatório de Análise de Produtos")
                .setFont(fontBold)
                .setFontSize(16)
                .setFontColor(primaryColor);
        document.add(sectionTitle);

        document.add(new Paragraph("\n")); // Space

        if (!allItems.isEmpty()) {
            // Create products table
            Table productsTable = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2 }))
                    .useAllAvailableWidth();

            // Header row
            productsTable.addHeaderCell(new Cell().add(new Paragraph("Produto")
                    .setFont(fontBold).setFontColor(primaryColor)));
            productsTable.addHeaderCell(new Cell().add(new Paragraph("Preço (€)")
                    .setFont(fontBold).setFontColor(primaryColor)));
            productsTable.addHeaderCell(new Cell().add(new Paragraph("Stock Atual")
                    .setFont(fontBold).setFontColor(primaryColor)));

            // Add products data
            for (Item item : allItems) {
                // Add product name
                productsTable.addCell(new Cell().add(new Paragraph(item.getNome())
                        .setFont(fontRegular)));

                // Add price
                productsTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(item.getPreco()))
                        .setFont(fontRegular)));

                // Add stock level
                int stock = item.getStockAtual();
                String stockText = stock > 0 ? String.valueOf(stock) : "N/A";
                productsTable.addCell(new Cell().add(new Paragraph(stockText).setFont(fontRegular)));
            }

            document.add(productsTable);
        } else {
            document.add(new Paragraph("Dados de produtos não disponíveis").setFont(fontRegular));
        }

        document.add(new Paragraph("\n\n")); // Space
    }

    /**
     * Add stock alerts report section to PDF
     */
    private void addStockAlertsReport(Document document, PdfFont fontBold, PdfFont fontRegular,
            DeviceRgb primaryColor, DeviceRgb lightGray) {
        // Section title
        Paragraph sectionTitle = new Paragraph("Relatório de Alertas de Stock")
                .setFont(fontBold)
                .setFontSize(16)
                .setFontColor(primaryColor);
        document.add(sectionTitle);

        document.add(new Paragraph("\n")); // Space

        // Get items with low stock
        List<Item> lowStockItems = allItems.stream()
                .filter(item -> item.getStockAtual() > 0 && item.getStockAtual() <= 10)
                .sorted((a, b) -> Integer.compare(a.getStockAtual(), b.getStockAtual()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (!lowStockItems.isEmpty()) {
            // Stock alerts introduction
            Paragraph stockIntro = new Paragraph("Items com níveis de estoque baixo que requerem atenção:")
                    .setFont(fontRegular)
                    .setFontSize(12);
            document.add(stockIntro);

            document.add(new Paragraph("\n")); // Space

            // Create stock alerts table
            Table stockTable = new Table(UnitValue.createPercentArray(new float[] { 5, 2, 2, 3 }))
                    .useAllAvailableWidth();

            // Header row
            stockTable.addHeaderCell(new Cell().add(new Paragraph("Produto")
                    .setFont(fontBold).setFontColor(primaryColor)));
            stockTable.addHeaderCell(new Cell().add(new Paragraph("Stock Atual")
                    .setFont(fontBold).setFontColor(primaryColor)));
            stockTable.addHeaderCell(new Cell().add(new Paragraph("Preço")
                    .setFont(fontBold).setFontColor(primaryColor)));
            stockTable.addHeaderCell(new Cell().add(new Paragraph("Status")
                    .setFont(fontBold).setFontColor(primaryColor)));

            // Add stock alerts data
            for (Item item : lowStockItems) {
                // Add product name
                stockTable.addCell(new Cell().add(new Paragraph(item.getNome())
                        .setFont(fontRegular)));

                // Add current stock
                stockTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getStockAtual()))
                        .setFont(fontRegular)));

                // Add price
                stockTable.addCell(new Cell().add(new Paragraph(currencyFormat.format(item.getPreco()))
                        .setFont(fontRegular)));

                // Add status
                String status = item.getStockAtual() <= 3 ? "CRÍTICO" : "BAIXO";
                Cell statusCell = new Cell().add(new Paragraph(status).setFont(fontBold));

                // Set cell background color based on status
                if (status.equals("CRÍTICO")) {
                    statusCell.setBackgroundColor(new DeviceRgb(255, 200, 200)); // light red
                } else {
                    statusCell.setBackgroundColor(new DeviceRgb(255, 229, 153)); // light yellow
                }

                stockTable.addCell(statusCell);
            }

            document.add(stockTable);
        } else {
            document.add(new Paragraph(
                    "Não há alertas de stock no momento. Todos os produtos têm níveis adequados de estoque.")
                    .setFont(fontRegular));
        }

        document.add(new Paragraph("\n\n")); // Space
    }

    /**
     * Dispose method to clean up resources when switching views
     */
    public void dispose() {
        // Clear data containers to help with memory management
        if (dailySalesData != null) {
            dailySalesData.clear();
        }
        if (dailyOrdersData != null) {
            dailyOrdersData.clear();
        }
        if (itemProfitData != null) {
            itemProfitData.clear();
        }
        if (allItems != null) {
            allItems.clear();
        }

        // Set data references to null
        dailySalesData = null;
        dailyOrdersData = null;
        itemProfitData = null;
        allItems = null;

        // Clear UI components
        if (contentArea != null) {
            Platform.runLater(() -> contentArea.getChildren().clear());
        }

        System.out.println("Limpeza de recursos da view de relatórios");
    }
}