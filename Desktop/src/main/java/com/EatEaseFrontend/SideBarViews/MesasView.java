package com.EatEaseFrontend.SideBarViews;

import com.EatEaseFrontend.AppConfig;
import com.EatEaseFrontend.AsyncOperationManager;
import com.EatEaseFrontend.Mesa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.EatEaseFrontend.JsonParser;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * View para gerenciar e exibir mesas do restaurante
 */
public class MesasView {

    private final StackPane contentArea;
    private final HttpClient httpClient;
    private final AsyncOperationManager asyncManager;
    private final AtomicBoolean isTimerOperationRunning = new AtomicBoolean(false);

    // Map to store mesa positions persistently
    private final Map<Integer, Double[]> mesaPositions = new HashMap<>();

    // Responsive layout variables
    private Pane currentMesasGrid;
    private double lastContainerWidth = 0;
    private double lastContainerHeight = 0;

    // Temporizador para atualizações automáticas
    private Timer autoUpdateTimer;
    // Intervalo de atualização em segundos (facilmente ajustável)
    private final int updateIntervalSeconds = 15; // Altere esse valor para modificar o intervalo de atualização
    // Flag para controlar se a view está ativa
    private boolean isViewActive = false;

    /**
     * Construtor da view de mesas
     * 
     * @param contentArea Área de conteúdo onde a view será exibida
     * @param httpClient  Cliente HTTP para fazer requisições à API
     */
    public MesasView(StackPane contentArea, HttpClient httpClient) {
        this.contentArea = contentArea;
        this.httpClient = httpClient;
        this.asyncManager = new AsyncOperationManager();
    }

    /**
     * Carrega e exibe a lista de mesas
     */
    public void show() {
        System.out.println("Carregando lista de mesas...");

        // Marcar a view como ativa
        isViewActive = true;

        // Show loading indicator
        contentArea.getChildren().clear();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Text loadingText = new Text("Carregando mesas...");
        loadingText.getStyleClass().add("welcome-text");
        VBox loadingBox = new VBox(20, progressIndicator, loadingText);
        loadingBox.setAlignment(Pos.CENTER);
        contentArea.getChildren().add(loadingBox);

        // Make API request to get mesas
        loadMesas();

        // Iniciar o temporizador de atualização automática se ainda não estiver ativo
        startAutoUpdateTimer();
    }

    /**
     * Carrega as mesas do servidor (CORRIGIDO)
     */
    private void loadMesas() {
        System.out.println("[MESAS] Iniciando carregamento de mesas...");

        // Verificar se é uma operação do timer para evitar conflitos
        boolean isTimerOperation = !Platform.isFxApplicationThread();

        // Para operações do timer, usar controle atômico
        if (isTimerOperation) {
            if (!isTimerOperationRunning.compareAndSet(false, true)) {
                System.out.println("[MESAS] Operação automática já em execução, ignorando...");
                return;
            }
        }

        // Criar requisição
        HttpRequest getMesasReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getAll")))
                .GET()
                .build();

        // Usar AsyncOperationManager para controlar a operação
        asyncManager.executeSimpleOperation(
                () -> {
                    try {
                        System.out.println("[MESAS] Enviando requisição síncrona para carregar mesas...");
                        HttpResponse<String> response = httpClient.send(getMesasReq,
                                HttpResponse.BodyHandlers.ofString());

                        System.out.println("[MESAS] Response status: " + response.statusCode());
                        if (response.statusCode() == 200) {
                            System.out.println("[MESAS] Mesas recebidas com sucesso");
                            return JsonParser.parseMesas(response.body());
                        } else {
                            System.err.println("[MESAS] Erro HTTP ao carregar mesas. Status: " + response.statusCode());
                            throw new RuntimeException("Failed to load mesas: " + response.statusCode());
                        }
                    } catch (Exception e) {
                        System.err.println("[MESAS] Exceção ao carregar mesas: " + e.getMessage());
                        throw new RuntimeException(e);
                    } finally {
                        // Reset do flag para operações do timer
                        if (isTimerOperation) {
                            isTimerOperationRunning.set(false);
                        }
                    }
                },
                () -> {
                    // onComplete - executado na UI thread
                    System.out.println("[MESAS] Carregamento completo, atualizando UI");
                }).thenAccept(mesas -> {
                    if (mesas != null) {
                        // Load positions from server first, then display mesas
                        loadMesaPositionsFromServer(mesas, () -> {
                            Platform.runLater(() -> displayMesasAsGrid(mesas));
                        });
                    }
                }).exceptionally(e -> {
                    System.err.println("[MESAS] Erro no carregamento assíncrono: " + e.getMessage());
                    Platform.runLater(() -> {
                        if (!isTimerOperation) { // Só mostrar erro para operações manuais
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Falha ao carregar mesas");
                            alert.setContentText("Erro: " + e.getMessage());
                            alert.showAndWait();
                        }
                    });
                    return null;
                });
    }

    /**
     * Inicia o temporizador para atualização automática das mesas (CORRIGIDO)
     */
    private void startAutoUpdateTimer() {
        // Cancelar qualquer temporizador existente
        stopAutoUpdateTimer();

        System.out.println(
                "[MESAS] Iniciando timer de atualização automática (intervalo: " + updateIntervalSeconds + "s)");

        // Criar um novo temporizador
        autoUpdateTimer = new Timer();
        autoUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Verificar se a view ainda está ativa antes de atualizar
                if (isViewActive) {
                    System.out.println("[MESAS] Executando atualização automática...");
                    // Carregar as mesas silenciosamente (sem mostrar indicador de carregamento)
                    silentlyUpdateMesas();
                } else {
                    System.out.println("[MESAS] View inativa, cancelando timer...");
                    this.cancel();
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
     * Atualiza as mesas silenciosamente (sem mostrar indicador de carregamento)
     * Inclui atualização das posições do servidor
     */
    private void silentlyUpdateMesas() {
        System.out.println("Realizando atualização automática das mesas (incluindo posições)...");
        HttpRequest getMesasReq = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getAll")))
                .GET()
                .build();

        httpClient.sendAsync(getMesasReq, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return JsonParser.parseMesas(response.body());
                    } else {
                        throw new RuntimeException("Failed to update mesas: " + response.statusCode());
                    }
                })
                .thenAccept(mesas -> {
                    // Só atualizar a UI se a view estiver ativa
                    if (isViewActive) {
                        System.out.println("Carregando posições atualizadas do servidor...");
                        // Primeiro carregar as posições do servidor, depois atualizar apenas os estados
                        loadMesaPositionsFromServer(mesas, () -> {
                            System.out.println(
                                    "Atualizando estado das mesas automaticamente preservando posições atualizadas");
                            Platform.runLater(() -> updateMesaStatesAndPositions(mesas));
                        });
                    }
                })
                .exceptionally(e -> {
                    // Falhas silenciosas, apenas log para console
                    System.err.println("Erro na atualização automática: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Busca as posições relativas das mesas do servidor (coordenadas 0-1)
     */
    private void loadMesaPositionsFromServer(List<Mesa> mesas, Runnable onComplete) {
        System.out.println("Carregando posições das mesas do servidor...");

        // Use a counter to track completed requests
        final int[] completedRequests = { 0 };
        final int totalRequests = mesas.size();

        if (totalRequests == 0) {
            if (onComplete != null) {
                onComplete.run();
            }
            return; // No mesas to load positions for
        }

        for (Mesa mesa : mesas) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getApiEndpoint("/mesa/getMesaPositionsById?mesaId=" + mesa.getId())))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            return response.body();
                        } else {
                            System.err.println(
                                    "Erro ao buscar posição da mesa " + mesa.getId() + ": " + response.statusCode());
                            return null;
                        }
                    })
                    .thenAccept(responseBody -> {
                        synchronized (this) {
                            if (responseBody != null && !responseBody.trim().isEmpty()) {
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    Map<String, Double> coords = mapper.readValue(responseBody,
                                            new TypeReference<>() {
                                            });
                                    double relativeX = coords.get("pos_x");
                                    double relativeY = coords.get("pos_y");

                                    // Store relative coordinates directly from server (0-1 scale)
                                    Double[] relativePos = new Double[] { relativeX, relativeY };
                                    mesaPositions.put(mesa.getId(), relativePos);

                                    System.out.println("Mesa " + mesa.getNumero() + " (ID: " + mesa.getId() +
                                            ") - Posição relativa carregada do servidor: (" +
                                            String.format("%.3f", relativeX) + ", " + String.format("%.3f", relativeY) +
                                            ") [0-1 scale]");

                                } catch (Exception e) {
                                    System.err.println(
                                            "Erro ao parsear posição da mesa " + mesa.getId() + ": " + e.getMessage());
                                }
                            }

                            completedRequests[0]++;
                            if (completedRequests[0] == totalRequests) {
                                System.out.println("Todas as posições das mesas foram carregadas do servidor");
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        }
                    })
                    .exceptionally(e -> {
                        synchronized (this) {
                            System.err.println(
                                    "Exceção ao buscar posição da mesa " + mesa.getId() + ": " + e.getMessage());
                            completedRequests[0]++;
                            if (completedRequests[0] == totalRequests) {
                                System.out.println("Carregamento de posições finalizado (com alguns erros)");
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        }
                        return null;
                    });
        }
    }

    /**
     * Salva a posição relativa de uma mesa no servidor (coordenadas 0-1)
     */
    private void saveMesaPositionToServer(int mesaId, double relativeX, double relativeY) {
        System.out.println("Salvando posição relativa da mesa " + mesaId + " no servidor: (" +
                String.format("%.3f", relativeX) + ", " + String.format("%.3f", relativeY) + ") [0-1 scale]");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/updatePosition?id=" + mesaId +
                        "&pos_x=" + relativeX + "&pos_y=" + relativeY)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("Posição relativa da mesa " + mesaId + " salva com sucesso no servidor");
                        return true;
                    } else {
                        System.err.println("Erro ao salvar posição da mesa " + mesaId + ": " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Exceção ao salvar posição da mesa " + mesaId + ": " + e.getMessage());
                    return false;
                });
    }

    /**
     * Atualiza apenas o estado das mesas sem recriar o layout (preserva posições)
     */
    private void updateMesaStatesOnly(List<Mesa> mesas) {
        if (currentMesasGrid == null || currentMesasGrid.getChildren().isEmpty()) {
            // Se não há layout existente, criar um novo
            displayMesasAsGrid(mesas);
            return;
        }

        // Criar um mapa para lookup rápido das mesas por ID
        Map<Integer, Mesa> mesaMap = new HashMap<>();
        for (Mesa mesa : mesas) {
            mesaMap.put(mesa.getId(), mesa);
        }

        // Atualizar apenas o estado visual das mesas existentes
        for (javafx.scene.Node node : currentMesasGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane mesaBox = (StackPane) node;
                try {
                    // Extrair o número da mesa do texto
                    VBox layout = (VBox) mesaBox.getChildren().get(0);
                    VBox infoContainer = (VBox) ((StackPane) layout.getChildren().get(0)).getChildren().get(1);
                    Text mesaNumberText = (Text) infoContainer.getChildren().get(1);
                    String text = mesaNumberText.getText(); // "Mesa X"
                    int mesaNumber = Integer.parseInt(text.split(" ")[1]);

                    // Assumindo que ID = numero para simplificação
                    Mesa updatedMesa = mesaMap.get(mesaNumber);
                    if (updatedMesa != null) {
                        // Atualizar apenas a cor baseada no estado
                        StackPane tableContainer = (StackPane) layout.getChildren().get(0);
                        Rectangle mesaRect = (Rectangle) tableContainer.getChildren().get(0);

                        Color mesaColor = updatedMesa.isEstadoLivre() ? Color.valueOf("#4CAF50")
                                : Color.valueOf("#F44336");
                        mesaRect.setFill(mesaColor);

                        // Atualizar texto do status se existir
                        if (infoContainer.getChildren().size() > 2) {
                            Text statusText = (Text) infoContainer.getChildren().get(2);
                            statusText.setText(updatedMesa.isEstadoLivre() ? "Livre" : "Ocupada");
                        }

                        System.out.println("Mesa " + mesaNumber + " - Estado atualizado: " +
                                (updatedMesa.isEstadoLivre() ? "Livre" : "Ocupada") +
                                " (posição preservada)");
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao atualizar estado da mesa: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Atualiza estados e posições das mesas durante atualizações automáticas
     */
    private void updateMesaStatesAndPositions(List<Mesa> mesas) {
        if (currentMesasGrid == null || currentMesasGrid.getChildren().isEmpty()) {
            // Se não há layout existente, criar um novo
            displayMesasAsGrid(mesas);
            return;
        }

        // Criar um mapa para lookup rápido das mesas por ID
        Map<Integer, Mesa> mesaMap = new HashMap<>();
        for (Mesa mesa : mesas) {
            mesaMap.put(mesa.getId(), mesa);
        }

        System.out.println("Atualizando posições e estados das mesas...");

        // Atualizar posições e estados das mesas existentes
        for (javafx.scene.Node node : currentMesasGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane mesaBox = (StackPane) node;
                try {
                    // Extrair o número da mesa do texto
                    VBox layout = (VBox) mesaBox.getChildren().get(0);
                    VBox infoContainer = (VBox) ((StackPane) layout.getChildren().get(0)).getChildren().get(1);
                    Text mesaNumberText = (Text) infoContainer.getChildren().get(1);
                    String text = mesaNumberText.getText(); // "Mesa X"
                    int mesaNumber = Integer.parseInt(text.split(" ")[1]);

                    Mesa updatedMesa = mesaMap.get(mesaNumber);
                    if (updatedMesa != null) {
                        // 1. Atualizar posição se mudou no servidor
                        Double[] serverPosition = mesaPositions.get(updatedMesa.getId());
                        if (serverPosition != null && isRelativePosition(serverPosition)) {
                            Double[] absolutePos = convertToAbsolutePosition(serverPosition[0], serverPosition[1]);
                            double newX = absolutePos[0];
                            double newY = absolutePos[1];

                            // Só mover se a posição mudou significativamente
                            double currentX = mesaBox.getLayoutX();
                            double currentY = mesaBox.getLayoutY();

                            if (Math.abs(currentX - newX) > 5 || Math.abs(currentY - newY) > 5) {
                                mesaBox.setLayoutX(newX);
                                mesaBox.setLayoutY(newY);
                                System.out.println("Mesa " + mesaNumber + " - Posição atualizada do servidor: (" +
                                        String.format("%.2f", newX) + ", " + String.format("%.2f", newY) + ")");
                            }
                        }

                        // 2. Atualizar estado visual (cor e texto)
                        StackPane tableContainer = (StackPane) layout.getChildren().get(0);
                        Rectangle mesaRect = (Rectangle) tableContainer.getChildren().get(0);

                        Color mesaColor = updatedMesa.isEstadoLivre() ? Color.valueOf("#4CAF50")
                                : Color.valueOf("#F44336");
                        mesaRect.setFill(mesaColor);

                        // Atualizar texto do status se existir
                        if (infoContainer.getChildren().size() > 2) {
                            Text statusText = (Text) infoContainer.getChildren().get(2);
                            statusText.setText(updatedMesa.isEstadoLivre() ? "Livre" : "Ocupada");
                        }

                        System.out.println("Mesa " + mesaNumber + " - Estado atualizado: " +
                                (updatedMesa.isEstadoLivre() ? "Livre" : "Ocupada"));
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao atualizar mesa: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Exibe as mesas em uma grade
     * 
     * @param mesas Lista de mesas a serem exibidas
     */
    private void displayMesasAsGrid(List<Mesa> mesas) {
        // Garantir que as mesas sejam sempre exibidas em ordem por número
        mesas.sort(Comparator.comparingInt(Mesa::getNumero));

        contentArea.getChildren().clear();

        // Header with title and refresh button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Gestão de Mesas");
        title.getStyleClass().add("welcome-text");

        Button refreshButton = new Button();
        FontIcon refreshIcon = new FontIcon(MaterialDesign.MDI_REFRESH);
        refreshIcon.setIconColor(Color.valueOf("#2a5298"));
        refreshIcon.setIconSize(20);
        refreshButton.setGraphic(refreshIcon);
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setTooltip(new Tooltip("Atualizar mesas"));
        refreshButton.setOnAction(e -> show());

        // Adicionar botão de nova mesa
        Button addButton = new Button();
        FontIcon addIcon = new FontIcon(MaterialDesign.MDI_PLUS);
        addIcon.setIconColor(Color.valueOf("#2a5298"));
        addIcon.setIconSize(20);
        addButton.setGraphic(addIcon);
        addButton.getStyleClass().add("icon-button");
        addButton.setTooltip(new Tooltip("Adicionar nova mesa"));
        addButton.setOnAction(e -> showAddMesaDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(title, spacer, addButton, refreshButton);

        // Create content with draggable layout
        Pane mesasGrid = new Pane();
        mesasGrid.setPadding(new Insets(20));
        currentMesasGrid = mesasGrid; // Store reference for responsive updates

        // Make the pane responsive
        mesasGrid.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (Math.abs(newWidth.doubleValue() - lastContainerWidth) > 50) { // Only respond to significant changes
                lastContainerWidth = newWidth.doubleValue();
                updateResponsiveLayout();
            }
        });

        mesasGrid.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (Math.abs(newHeight.doubleValue() - lastContainerHeight) > 50) { // Only respond to significant changes
                lastContainerHeight = newHeight.doubleValue();
                updateResponsiveLayout();
            }
        });

        // Convert any existing absolute positions to relative
        convertExistingPositionsToRelative();

        // Add mesa cards to grid with persistent positions
        for (int i = 0; i < mesas.size(); i++) {
            Mesa mesa = mesas.get(i);
            StackPane mesaBox = createMesaBox(mesa);

            // Check if we have a stored position for this mesa
            Double[] storedPosition = mesaPositions.get(mesa.getId());
            double x, y;

            if (storedPosition != null && isRelativePosition(storedPosition)) {
                // Convert relative position to absolute based on current container size
                Double[] absolutePos = convertToAbsolutePosition(storedPosition[0], storedPosition[1]);
                x = absolutePos[0];
                y = absolutePos[1];
                System.out.println("Mesa " + mesa.getNumero() + " - Restored relative position: (" +
                        String.format("%.3f", storedPosition[0]) + ", " + String.format("%.3f", storedPosition[1]) +
                        ") [relative] = (" + String.format("%.2f", x) + ", " + String.format("%.2f", y)
                        + ") [absolute]");
            } else {
                // Calculate initial position for new mesas
                int columns = calculateOptimalColumns();
                int xOffset = 50;
                int yOffset = 50;
                int cardWidth = 220; // Width including spacing
                int cardHeight = 240; // Height including spacing

                int row = i / columns;
                int col = i % columns;
                x = xOffset + (col * cardWidth);
                y = yOffset + (row * cardHeight);

                // Store the initial position as relative coordinates
                Double[] relativePos = convertToRelativePosition(x, y);
                mesaPositions.put(mesa.getId(), relativePos);
                System.out.println("Mesa " + mesa.getNumero() + " - Initial position: (" +
                        String.format("%.2f", x) + ", " + String.format("%.2f", y) +
                        ") [absolute] = (" + String.format("%.3f", relativePos[0]) + ", " +
                        String.format("%.3f", relativePos[1]) + ") [relative]");
            }

            // Set position
            mesaBox.setLayoutX(x);
            mesaBox.setLayoutY(y);

            // Make mesa draggable
            makeMesaDraggable(mesaBox, mesa);

            mesasGrid.getChildren().add(mesaBox);
        }

        // Add everything to a scroll pane
        ScrollPane scrollPane = new ScrollPane(mesasGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.getChildren().addAll(header, scrollPane);

        contentArea.getChildren().add(mainLayout);
    }

    /**
     * Cria uma representação visual da mesa
     * 
     * @param mesa Mesa a ser representada
     * @return StackPane contendo a representação visual da mesa
     */
    private StackPane createMesaBox(Mesa mesa) {
        // Create the mesa box
        StackPane mesaBox = new StackPane();
        mesaBox.setPrefSize(200, 200);
        mesaBox.getStyleClass().add("dashboard-card");

        // Background color based on state
        Color mesaColor = mesa.isEstadoLivre() ? Color.valueOf("#4CAF50") : Color.valueOf("#F44336");

        // Mesa table visual (rectangle with rounded corners)
        Rectangle mesaRect = new Rectangle(160, 120);
        mesaRect.setFill(mesaColor);
        mesaRect.setArcWidth(20);
        mesaRect.setArcHeight(20);
        mesaRect.setStroke(Color.valueOf("#37474F"));
        mesaRect.setStrokeWidth(2);

        // Mesa information
        Text idText = new Text("ID: " + mesa.getId());
        idText.setFill(Color.WHITE);
        idText.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Text mesaNumber = new Text("Mesa " + mesa.getNumero());
        mesaNumber.setFill(Color.WHITE);
        mesaNumber.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Mesa status
        Text statusText = new Text(mesa.isEstadoLivre() ? "Livre" : "Ocupada");
        statusText.setFill(Color.WHITE);
        statusText.setFont(Font.font("System", 14));

        // Stack information on top of rectangle
        StackPane stackedInfo = new StackPane();
        stackedInfo.getChildren().add(mesaRect);

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(idText, mesaNumber, statusText);
        stackedInfo.getChildren().add(infoBox);

        // Button Container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);

        // Action buttons
        Button liberarButton = new Button("Liberar");
        liberarButton.getStyleClass().add("green-button");
        liberarButton.setDisable(mesa.isEstadoLivre());

        Button ocuparButton = new Button("Ocupar");
        ocuparButton.getStyleClass().add("red-button");
        ocuparButton.setDisable(!mesa.isEstadoLivre());

        buttonContainer.getChildren().addAll(liberarButton, ocuparButton);

        // Button actions
        liberarButton.setOnAction(e -> {
            showConfirmDialog("Liberar Mesa", "Tem certeza que deseja liberar a Mesa " + mesa.getNumero() + "?", () -> {
                liberarMesa(mesa.getId());
            });
        });

        ocuparButton.setOnAction(e -> {
            showConfirmDialog("Ocupar Mesa",
                    "Tem certeza que deseja marcar a Mesa " + mesa.getNumero() + " como ocupada?", () -> {
                        ocuparMesa(mesa.getId());
                    });
        });

        // Arrange elements vertically
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(stackedInfo, buttonContainer);

        mesaBox.getChildren().add(layout);
        return mesaBox;
    }

    /**
     * Exibe uma caixa de diálogo de confirmação
     * 
     * @param title     Título da caixa de diálogo
     * @param message   Mensagem a ser exibida
     * @param onConfirm Ação a ser executada ao confirmar
     */
    private void showConfirmDialog(String title, String message, Runnable onConfirm) {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create main container
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        container.setPrefWidth(400);
        container.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setAlignment(Pos.CENTER);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        messageLabel.setAlignment(Pos.CENTER);

        // Buttons
        Button confirmButton = new Button("Confirmar");
        confirmButton.getStyleClass().add("login-button");
        confirmButton.setOnAction(e -> {
            popup.hide();
            onConfirm.run();
        });

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("login-button");
        cancelButton.setOnAction(e -> popup.hide());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        container.getChildren().addAll(titleLabel, messageLabel, buttonBox);
        popup.getContent().add(container);

        // Show popup centered
        double centerX = contentArea.getScene().getWindow().getX() + contentArea.getScene().getWindow().getWidth() / 2;
        double centerY = contentArea.getScene().getWindow().getY() + contentArea.getScene().getWindow().getHeight() / 2;
        popup.show(contentArea.getScene().getWindow(), centerX - 200, centerY - 100);
    }

    /**
     * Envia requisição para liberar uma mesa
     * 
     * @param mesaId ID da mesa a ser liberada
     */
    private void liberarMesa(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/liberar?id=" + mesaId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao liberar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa liberada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao liberar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Envia requisição para ocupar uma mesa
     * 
     * @param mesaId ID da mesa a ser ocupada
     */
    private void ocuparMesa(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/ocupar?id=" + mesaId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao ocupar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa ocupada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao ocupar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Exibe uma caixa de alerta
     * 
     * @param message   Mensagem a ser exibida
     * @param alertType Tipo do alerta
     */
    private void showAlert(String message, Alert.AlertType alertType) {
        String title = alertType == Alert.AlertType.ERROR ? "Erro" : "Sucesso";
        PopUp.showPopupDialog(alertType, title, "", message);
    }

    /**
     * Exibe o diálogo para adicionar uma nova mesa
     */
    private void showAddMesaDialog() {
        // Create popup
        Popup popup = new Popup();
        popup.setAutoHide(false);

        // Create main container
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10,0,0,4);");
        container.setPrefWidth(400);
        container.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Adicionar Nova Mesa");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setAlignment(Pos.CENTER);

        // Form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // Add the numero field
        TextField numeroField = new TextField();
        numeroField.setPromptText("Número da Mesa");
        grid.add(new Label("Número:"), 0, 0);
        grid.add(numeroField, 1, 0);

        // Buttons
        Button saveButton = new Button("Salvar");
        saveButton.getStyleClass().add("login-button");
        saveButton.setDisable(true);

        Button cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("login-button");
        cancelButton.setOnAction(e -> popup.hide());

        // Validate input - only numbers allowed
        numeroField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = false;
            try {
                if (!newValue.isEmpty()) {
                    int numero = Integer.parseInt(newValue);
                    isValid = numero > 0;
                }
            } catch (NumberFormatException e) {
                isValid = false;
            }
            saveButton.setDisable(!isValid);
        });

        saveButton.setOnAction(e -> {
            try {
                int numero = Integer.parseInt(numeroField.getText());
                popup.hide();
                createMesa(numero);
            } catch (NumberFormatException ex) {
                // Should not happen due to validation
            }
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        container.getChildren().addAll(titleLabel, grid, buttonBox);
        popup.getContent().add(container);

        // Show popup centered
        double centerX = contentArea.getScene().getWindow().getX() + contentArea.getScene().getWindow().getWidth() / 2;
        double centerY = contentArea.getScene().getWindow().getY() + contentArea.getScene().getWindow().getHeight() / 2;
        popup.show(contentArea.getScene().getWindow(), centerX - 200, centerY - 100);

        // Request focus on the numero field
        Platform.runLater(numeroField::requestFocus);
    }

    /**
     * Envia requisição para criar uma nova mesa
     * 
     * @param numero Número da nova mesa
     */
    private void createMesa(int numero) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getApiEndpoint("/mesa/create?numero=" + numero)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Response status: " + response.statusCode());
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return true;
                    } else {
                        throw new RuntimeException("Falha ao criar mesa: " + response.statusCode());
                    }
                })
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        // Reload mesas after action
                        show();
                        showAlert("Mesa criada com sucesso!", Alert.AlertType.INFORMATION);
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        showAlert("Erro ao criar mesa: " + e.getMessage(), Alert.AlertType.ERROR);
                    });
                    e.printStackTrace();
                    return null;
                });
    }

    /**
     * Método para limpar recursos quando a view não está mais visível
     * Este método deve ser chamado quando a view for fechada ou substituída
     */
    public void dispose() {
        // Verificar se a view já está inativa para evitar operações duplicadas
        if (isViewActive) {
            System.out.println("Desativando atualização automática das mesas");
            isViewActive = false;
            stopAutoUpdateTimer();
        }
    }

    /**
     * Calcula o número ideal de colunas baseado no tamanho da janela
     * 
     * @return Número de colunas para o layout inicial
     */
    private int calculateOptimalColumns() {
        double availableWidth = contentArea.getWidth();
        if (availableWidth <= 0) {
            availableWidth = 1000; // Default fallback
        }

        int cardWidth = 220; // Width including spacing
        int padding = 100; // Total padding (left + right)
        int minColumns = 2;
        int maxColumns = 6;

        int columns = Math.max(minColumns, (int) ((availableWidth - padding) / cardWidth));
        return Math.min(columns, maxColumns);
    }

    /**
     * Atualiza o layout de forma responsiva quando o tamanho da janela muda
     */
    private void updateResponsiveLayout() {
        if (currentMesasGrid == null || currentMesasGrid.getChildren().isEmpty()) {
            return;
        }

        // Update all mesa positions from relative to absolute coordinates based on new
        // container size
        for (javafx.scene.Node node : currentMesasGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane mesaBox = (StackPane) node;

                // Find the mesa associated with this box
                Mesa mesa = findMesaForBox(mesaBox);
                if (mesa != null) {
                    Double[] relativePos = mesaPositions.get(mesa.getId());
                    if (relativePos != null && isRelativePosition(relativePos)) {
                        // Convert relative position to new absolute position
                        Double[] absolutePos = convertToAbsolutePosition(relativePos[0], relativePos[1]);
                        mesaBox.setLayoutX(absolutePos[0]);
                        mesaBox.setLayoutY(absolutePos[1]);

                        System.out.println("Mesa " + mesa.getNumero() + " - Responsive update: (" +
                                String.format("%.3f", relativePos[0]) + ", " + String.format("%.3f", relativePos[1]) +
                                ") [relative] = (" + String.format("%.2f", absolutePos[0]) + ", " +
                                String.format("%.2f", absolutePos[1]) + ") [absolute]");
                    }
                }
            }
        }
    }

    /**
     * Encontra a mesa associada a um StackPane
     */
    private Mesa findMesaForBox(StackPane mesaBox) {
        try {
            VBox layout = (VBox) mesaBox.getChildren().get(0);
            VBox infoContainer = (VBox) ((StackPane) layout.getChildren().get(0)).getChildren().get(1);
            Text mesaNumberText = (Text) infoContainer.getChildren().get(1);
            String text = mesaNumberText.getText(); // "Mesa X"
            int mesaNumber = Integer.parseInt(text.split(" ")[1]);

            // This is a simplified approach - in a real implementation you'd want to store
            // mesa references properly
            // For now, we assume mesa ID matches mesa number
            return new Mesa(mesaNumber, mesaNumber, true); // Dummy mesa for ID lookup
        } catch (Exception e) {
            System.err.println("Error finding mesa for box: " + e.getMessage());
            return null;
        }
    }

    /**
     * Torna uma mesa arrastável e salva posições relativas
     * 
     * @param mesaBox O StackPane da mesa a ser tornada arrastável
     * @param mesa    A mesa associada ao StackPane
     */
    private void makeMesaDraggable(StackPane mesaBox, Mesa mesa) {
        // Variables to store initial mouse position
        final double[] mouseX = new double[1];
        final double[] mouseY = new double[1];
        final double[] initialX = new double[1];
        final double[] initialY = new double[1];

        // Mouse pressed event - record initial positions
        mesaBox.setOnMousePressed(event -> {
            mouseX[0] = event.getSceneX();
            mouseY[0] = event.getSceneY();
            initialX[0] = mesaBox.getLayoutX();
            initialY[0] = mesaBox.getLayoutY();

            // Change cursor to indicate dragging
            mesaBox.setStyle(mesaBox.getStyle() + "; -fx-cursor: move;");

            System.out.println("Mesa " + mesa.getNumero() + " - Drag started at: (" +
                    String.format("%.2f", initialX[0]) + ", " +
                    String.format("%.2f", initialY[0]) + ")");
        });

        // Mouse dragged event - update position
        mesaBox.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mouseX[0];
            double deltaY = event.getSceneY() - mouseY[0];

            double newX = initialX[0] + deltaX;
            double newY = initialY[0] + deltaY;

            // Constrain to parent bounds
            if (mesaBox.getParent() instanceof Pane) {
                Pane parent = (Pane) mesaBox.getParent();
                double maxX = parent.getWidth() - mesaBox.getWidth();
                double maxY = parent.getHeight() - mesaBox.getHeight();

                if (maxX > 0 && maxY > 0) {
                    newX = Math.max(20, Math.min(newX, maxX - 20));
                    newY = Math.max(20, Math.min(newY, maxY - 20));
                }
            }

            // Update position
            mesaBox.setLayoutX(newX);
            mesaBox.setLayoutY(newY);
        });

        // Mouse released event - save relative position
        mesaBox.setOnMouseReleased(event -> {
            // Reset cursor
            mesaBox.setStyle(mesaBox.getStyle().replace("; -fx-cursor: move;", ""));

            double finalX = mesaBox.getLayoutX();
            double finalY = mesaBox.getLayoutY();

            // Convert to relative position and store
            Double[] relativePos = convertToRelativePosition(finalX, finalY);
            mesaPositions.put(mesa.getId(), relativePos);

            // Save relative position to server (0-1 scale for responsiveness)
            saveMesaPositionToServer(mesa.getId(), relativePos[0], relativePos[1]);

            System.out.println("Mesa " + mesa.getNumero() + " - Final position: (" +
                    String.format("%.2f", finalX) + ", " + String.format("%.2f", finalY) +
                    ") [absolute] = (" + String.format("%.3f", relativePos[0]) + ", " +
                    String.format("%.3f", relativePos[1]) + ") [relative]");
        });
    }

    /**
     * Converte coordenadas absolutas para relativas (0-1) baseadas no tamanho do
     * container
     * 
     * @param absoluteX Coordenada X absoluta em pixels
     * @param absoluteY Coordenada Y absoluta em pixels
     * @return Array com [relativeX, relativeY] em escala 0-1
     */
    private Double[] convertToRelativePosition(double absoluteX, double absoluteY) {
        if (currentMesasGrid == null) {
            return new Double[] { 0.1, 0.1 }; // Posição relativa padrão
        }

        double containerWidth = currentMesasGrid.getWidth();
        double containerHeight = currentMesasGrid.getHeight();

        // Se o container ainda não tem tamanho definido, usar dimensões da janela
        if (containerWidth <= 0)
            containerWidth = contentArea.getWidth();
        if (containerHeight <= 0)
            containerHeight = contentArea.getHeight();

        // Usar dimensões padrão se ainda não estiverem disponíveis
        if (containerWidth <= 0)
            containerWidth = 1000;
        if (containerHeight <= 0)
            containerHeight = 800;

        // Calcular posições relativas (0-1)
        double relativeX = absoluteX / containerWidth;
        double relativeY = absoluteY / containerHeight;

        // Garantir que permaneçam dentro dos limites 0-1
        relativeX = Math.max(0.0, Math.min(1.0, relativeX));
        relativeY = Math.max(0.0, Math.min(1.0, relativeY));

        return new Double[] { relativeX, relativeY };
    }

    /**
     * Converte coordenadas relativas (0-1) para absolutas baseadas no tamanho do
     * container
     * 
     * @param relativeX Coordenada X relativa (0-1)
     * @param relativeY Coordenada Y relativa (0-1)
     * @return Array com [absoluteX, absoluteY]
     */
    private Double[] convertToAbsolutePosition(double relativeX, double relativeY) {
        if (currentMesasGrid == null) {
            return new Double[] { 50.0, 50.0 }; // Posição padrão
        }

        double containerWidth = currentMesasGrid.getWidth();
        double containerHeight = currentMesasGrid.getHeight();

        // Se o container ainda não tem tamanho definido, usar dimensões padrão
        if (containerWidth <= 0)
            containerWidth = 1000;
        if (containerHeight <= 0)
            containerHeight = 800;

        // Calcular posições absolutas, garantindo que fiquem dentro dos limites
        double mesaBoxWidth = 200; // Largura aproximada do mesa box
        double mesaBoxHeight = 200; // Altura aproximada do mesa box
        double margin = 20;

        double maxX = containerWidth - mesaBoxWidth - margin;
        double maxY = containerHeight - mesaBoxHeight - margin;

        double absoluteX = Math.max(margin, Math.min(maxX, relativeX * containerWidth));
        double absoluteY = Math.max(margin, Math.min(maxY, relativeY * containerHeight));

        return new Double[] { absoluteX, absoluteY };
    }

    /**
     * Verifica se uma posição armazenada é relativa (valores entre 0-1) ou absoluta
     * 
     * @param position Array com [x, y]
     * @return true se for posição relativa, false se for absoluta
     */
    private boolean isRelativePosition(Double[] position) {
        if (position == null || position.length != 2) {
            return false;
        }
        // Se ambos os valores estão entre 0 e 1, assumimos que é posição relativa
        return position[0] >= 0.0 && position[0] <= 1.0 &&
                position[1] >= 0.0 && position[1] <= 1.0;
    }

    /**
     * Converte todas as posições absolutas antigas para relativas
     */
    private void convertExistingPositionsToRelative() {
        if (currentMesasGrid == null)
            return;

        Map<Integer, Double[]> convertedPositions = new HashMap<>();

        for (Map.Entry<Integer, Double[]> entry : mesaPositions.entrySet()) {
            Double[] position = entry.getValue();
            if (!isRelativePosition(position)) {
                // Converter posição absoluta para relativa
                Double[] relativePos = convertToRelativePosition(position[0], position[1]);
                convertedPositions.put(entry.getKey(), relativePos);
                System.out.println("Convertendo posição da Mesa ID " + entry.getKey() +
                        " de (" + String.format("%.2f", position[0]) + ", " + String.format("%.2f", position[1]) +
                        ") para (" + String.format("%.3f", relativePos[0]) + ", "
                        + String.format("%.3f", relativePos[1]) + ") [relativa]");
            } else {
                convertedPositions.put(entry.getKey(), position);
            }
        }

        // Substituir as posições antigas pelas convertidas
        mesaPositions.clear();
        mesaPositions.putAll(convertedPositions);
    }
}
